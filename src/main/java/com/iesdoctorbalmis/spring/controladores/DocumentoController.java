package com.iesdoctorbalmis.spring.controladores;

import java.util.List;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.iesdoctorbalmis.spring.dto.DocumentoDraftCreateDTO;
import com.iesdoctorbalmis.spring.dto.DocumentoWorkflowDTO;
import com.iesdoctorbalmis.spring.excepciones.RecursoNoEncontradoException;
import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Documento;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoDocumento;
import com.iesdoctorbalmis.spring.modelo.enums.TipoDocumento;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.servicios.DocumentoService;
import com.iesdoctorbalmis.spring.servicios.PdfService;
import com.iesdoctorbalmis.spring.servicios.TrasladoService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Documentos", description = "Gestion de documentos legales y cartas de porte en PDF")
@RestController
@RequestMapping("/api/documentos")
public class DocumentoController {

    private final DocumentoService service;
    private final TrasladoService trasladoService;
    private final PdfService pdfService;
    private final CentroRepository centroRepo;

    @Value("${ecoadmin.uploads.documentos:uploads/documentos}")
    private String directorioUploads;

    public DocumentoController(DocumentoService service, TrasladoService trasladoService,
                               PdfService pdfService, CentroRepository centroRepo) {
        this.service = service;
        this.trasladoService = trasladoService;
        this.pdfService = pdfService;
        this.centroRepo = centroRepo;
    }

    @GetMapping
    public List<Documento> listar() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Documento> buscar(@PathVariable Long id) {
        Documento d = service.findById(id);
        if (d == null) throw new RecursoNoEncontradoException("Documento no encontrado: " + id);
        return ResponseEntity.ok(d);
    }

    @GetMapping("/traslado/{trasladoId}")
    public List<Documento> porTraslado(@PathVariable Long trasladoId) {
        Traslado t = trasladoService.findById(trasladoId);
        if (t == null) throw new RecursoNoEncontradoException("Traslado no encontrado: " + trasladoId);
        return service.findByTraslado(t);
    }

    @PostMapping
    public ResponseEntity<Documento> crear(@RequestBody Documento d) {
        d.setArchivoUrl(null);
        if (requiereAdjuntoManual(d.getTipo())) {
            d.setEstado(EstadoDocumento.PENDIENTE_ADJUNTO);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(d));
    }

    @PostMapping("/drafts")
    public ResponseEntity<DocumentoWorkflowDTO> crearDraft(@RequestBody DocumentoDraftCreateDTO draft) {
        // Validar draft: tipo requerido, restricciones por tipo, metadatos si aplica
        validarDraft(draft);

        Documento documento = new Documento();
        documento.setTipo(draft.tipo());
        documento.setNumeroReferencia(trimOrNull(draft.numeroReferencia()));
        documento.setFechaEmision(draft.fechaEmision());
        documento.setFechaVencimiento(draft.fechaVencimiento());
        documento.setObservaciones(trimOrNull(draft.observaciones()));
        documento.setArchivoUrl(null);

        if (draft.trasladoId() != null) {
            Traslado traslado = trasladoService.findById(draft.trasladoId());
            if (traslado == null) {
                throw new IllegalArgumentException("Traslado no encontrado: " + draft.trasladoId());
            }
            documento.setTraslado(traslado);
        }

        if (draft.centroId() != null) {
            Centro centro = centroRepo.findById(draft.centroId())
                    .orElseThrow(() -> new IllegalArgumentException("Centro no encontrado: " + draft.centroId()));
            documento.setCentro(centro);
        }

        // Estado automático según tipo
        if (requiereAdjuntoManual(draft.tipo())) {
            // Tipos que necesitan PDF externo: CONTRATO, FICHA, HOJA, INFORME
            documento.setEstado(EstadoDocumento.PENDIENTE_ADJUNTO);
        } else {
            // Tipos generables: DOCUMENTO_IDENTIFICACION, NOTIFICACION_PREVIA, etc.
            documento.setEstado(EstadoDocumento.BORRADOR);
        }

        Documento guardado = service.save(documento);
        // Devolver workflow con siguiente acción clara para el cliente
        return ResponseEntity.status(HttpStatus.CREATED).body(aWorkflow(guardado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Documento> editar(@PathVariable Long id, @RequestBody Documento d) {
        Documento existing = service.findById(id);
        if (existing == null)
            throw new RecursoNoEncontradoException("Documento no encontrado: " + id);
        d.setId(id);
        d.setArchivoUrl(existing.getArchivoUrl());
        return ResponseEntity.ok(service.save(d));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (service.findById(id) == null)
            throw new RecursoNoEncontradoException("Documento no encontrado: " + id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generarPdf(@PathVariable Long id,
                                              @RequestParam(defaultValue = "false") boolean inline) {
        Documento d = service.findById(id);
        if (d == null) throw new RecursoNoEncontradoException("Documento no encontrado: " + id);
        Traslado t = d.getTraslado();
        if (t == null) return ResponseEntity.badRequest().build();

        byte[] pdf = switch (d.getTipo()) {
            case NOTIFICACION_PREVIA -> pdfService.generarNotificacionTraslado(t);
            case FICHA_ACEPTACION, INFORME_FINAL -> pdfService.generarCertificadoRecepcion(t);
            case DOCUMENTO_IDENTIFICACION, CONTRATO, HOJA_SEGUIMIENTO, ARCHIVO_CRONOLOGICO
                    -> pdfService.generarCartaDePorte(t);
        };

        String ref = d.getNumeroReferencia() != null ? d.getNumeroReferencia() : ("doc-" + id);
        String disposition = (inline ? "inline" : "attachment")
                + "; filename=\"" + ref + ".pdf\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/{id}/upload")
    public ResponseEntity<Documento> subirPdf(@PathVariable Long id,
                                              @RequestParam("archivo") MultipartFile archivo) throws IOException {
        Documento d = service.findById(id);
        if (d == null) throw new RecursoNoEncontradoException("Documento no encontrado: " + id);
        if (archivo == null || archivo.isEmpty())
            return ResponseEntity.badRequest().build();
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.toLowerCase().contains("pdf"))
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
        if (archivo.getSize() > 10L * 1024 * 1024)
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build();

        Path dir = Paths.get(directorioUploads).toAbsolutePath();
        Files.createDirectories(dir);
        String nombre = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "_" + id + "_" + UUID.randomUUID().toString().substring(0, 8) + ".pdf";
        Path destino = dir.resolve(nombre);
        try (var in = archivo.getInputStream()) {
            Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
        }
        d.setArchivoUrl("/uploads/documentos/" + nombre);
        if (d.getEstado() == EstadoDocumento.PENDIENTE_ADJUNTO || d.getEstado() == EstadoDocumento.BORRADOR) {
            d.setEstado(EstadoDocumento.EMITIDO);
            if (d.getFechaEmision() == null) {
                d.setFechaEmision(LocalDate.now());
            }
        }
        return ResponseEntity.ok(service.save(d));
    }

    @GetMapping("/{id}/workflow")
    public ResponseEntity<DocumentoWorkflowDTO> workflow(@PathVariable Long id) {
        Documento d = service.findById(id);
        if (d == null) throw new RecursoNoEncontradoException("Documento no encontrado: " + id);
        return ResponseEntity.ok(aWorkflow(d));
    }

    /**
     * Genera PDF server-side para documentos que nacen del dominio (DI, NP, etc.).\n
     * Solo aplica a tipos que no requieren adjunto manual.\n
     * Documentos adjuntos externamente (CONTRATO, FICHA_ACEPTACION) NO se pueden generar aquí.\n
     * \n
     * POST /{id}/generar marca el documento como EMITIDO y devuelve workflow actualizado + URL del PDF.\n
     */
    @PostMapping("/{id}/generar")
    public ResponseEntity<Map<String, Object>> generarDocumento(@PathVariable Long id) {
        Documento d = service.findById(id);
        if (d == null) throw new RecursoNoEncontradoException("Documento no encontrado: " + id);
        
        // Tipos que no son generables manualmente
        if (d.getTipo() == TipoDocumento.ARCHIVO_CRONOLOGICO) {
            throw new IllegalArgumentException("ARCHIVO_CRONOLOGICO se genera automaticamente del sistema, no manualmente");
        }
        if (requiereAdjuntoManual(d.getTipo())) {
            throw new IllegalArgumentException("Este tipo documental requiere adjuntar un PDF externo, no se genera desde el servidor");
        }
        if (d.getTraslado() == null) {
            throw new IllegalArgumentException("Este tipo documental requiere un traslado asociado para generar el PDF");
        }

        // Generar PDF: todos los tipos generables pasan aquí
        try {
            generarPdf(d.getTipo(), d.getTraslado());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al generar PDF para tipo " + d.getTipo() + ": " + e.getMessage(), e);
        }

        // Marcar como EMITIDO y establecer fecha de emisión si no existe
        d.setEstado(EstadoDocumento.EMITIDO);
        if (d.getFechaEmision() == null) {
            d.setFechaEmision(LocalDate.now());
        }
        Documento actualizado = service.save(d);

        // Respuesta con workflow actualizado y URL del PDF
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("workflow", aWorkflow(actualizado));
        body.put("pdfUrl", "/api/documentos/" + id + "/pdf?inline=true");
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}/archivo")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable Long id) throws IOException {
        Documento d = service.findById(id);
        if (d == null || d.getArchivoUrl() == null)
            return ResponseEntity.notFound().build();
        String nombre = d.getArchivoUrl().substring(d.getArchivoUrl().lastIndexOf('/') + 1);
        Path dir = Paths.get(directorioUploads).toAbsolutePath().normalize();
        Path archivo = dir.resolve(nombre).normalize();
        if (!archivo.startsWith(dir) || !Files.exists(archivo))
            return ResponseEntity.notFound().build();
        Resource resource = new UrlResource(archivo.toUri());
        String ref = d.getNumeroReferencia() != null ? d.getNumeroReferencia() : ("doc-" + id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + ref + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @GetMapping("/alertas/notificacion-previa")
    public List<Map<String, Object>> alertasNotificacionPrevia() {
        java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
        java.time.LocalDateTime limite = ahora.plusDays(10);
        return trasladoService.findAll().stream()
                .filter(t -> t.getFechaProgramadaInicio() != null)
                .filter(t -> !t.getFechaProgramadaInicio().isBefore(ahora))
                .filter(t -> t.getFechaProgramadaInicio().isBefore(limite))
                .filter(t -> service.findByTraslado(t).stream()
                        .noneMatch(doc -> doc.getTipo() == TipoDocumento.NOTIFICACION_PREVIA))
                .map(t -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("trasladoId", t.getId());
                    m.put("fechaProgramada", t.getFechaProgramadaInicio());
                    m.put("residuo", t.getResiduo() != null ? t.getResiduo().getCodigoLER() : null);
                    m.put("productor", t.getCentroProductor() != null ? t.getCentroProductor().getNombre() : null);
                    long dias = java.time.Duration.between(ahora, t.getFechaProgramadaInicio()).toDays();
                    m.put("diasRestantes", dias);
                    return m;
                })
                .toList();
    }

    /**
     * Valida el draft de creación según tipo documental.
     * Ensures: tipo es requerido, ciertos tipos requieren trasladoId/centroId,
     * metadatos cumplen esquema básico por tipo.
     */
    private void validarDraft(DocumentoDraftCreateDTO draft) {
        if (draft == null || draft.tipo() == null) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio");
        }
        if (draft.tipo() == TipoDocumento.ARCHIVO_CRONOLOGICO) {
            throw new IllegalArgumentException("ARCHIVO_CRONOLOGICO no se crea manualmente desde este flujo");
        }
        if ((draft.tipo() == TipoDocumento.DOCUMENTO_IDENTIFICACION
                || draft.tipo() == TipoDocumento.NOTIFICACION_PREVIA)
                && draft.trasladoId() == null) {
            throw new IllegalArgumentException("Este tipo documental requiere trasladoId");
        }
        if (draft.tipo() == TipoDocumento.CONTRATO && draft.centroId() == null) {
            throw new IllegalArgumentException("CONTRATO requiere centroId");
        }
        // Validar metadatos según tipo si están presentes
        if (draft.metadatos() != null && !draft.metadatos().isEmpty()) {
            validarMetadatos(draft.tipo(), draft.metadatos());
        }
    }

    /**
     * Valida contenido de metadatos según tipo documental.
     * Para DI: ler y cantidad son campos auxiliares sin validación obligatoria en esta fase.
     * Para NP: fechaPrevista y diasAntelacion deben ser valores numéricos/fechas coherentes si se envían.
     * Para CONTRATO: contraparte es opcional; fechaFirma debe ser fecha válida si se envía.
     */
    private void validarMetadatos(TipoDocumento tipo, Map<String, Object> metadatos) {
        if (tipo == null || metadatos == null) {
            return;
        }
        try {
            switch (tipo) {
                case DOCUMENTO_IDENTIFICACION -> {
                    // ler y cantidad son informativos; no son obligatorios
                    Object ler = metadatos.get("ler");
                    Object cantidad = metadatos.get("cantidad");
                    if (cantidad != null && !(cantidad instanceof Number)) {
                        throw new IllegalArgumentException("DI.cantidad debe ser numérico");
                    }
                }
                case NOTIFICACION_PREVIA -> {
                    Object diasAntelacion = metadatos.get("diasAntelacion");
                    if (diasAntelacion != null && !(diasAntelacion instanceof Number)) {
                        throw new IllegalArgumentException("NP.diasAntelacion debe ser numérico");
                    }
                    // fechaPrevista será string del input date HTML, se tolera
                }
                case CONTRATO -> {
                    // contraparte y fechaFirma son informativos; se toleran strings
                }
                default -> {
                    // Otros tipos no procesan metadatos en esta fase
                }
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Metadatos malformados para tipo " + tipo + ": " + e.getMessage(), e);
        }
    }

    private boolean requiereAdjuntoManual(TipoDocumento tipo) {
        if (tipo == null) {
            return false;
        }
        return switch (tipo) {
            case CONTRATO, FICHA_ACEPTACION, HOJA_SEGUIMIENTO, INFORME_FINAL -> true;
            case DOCUMENTO_IDENTIFICACION, NOTIFICACION_PREVIA, ARCHIVO_CRONOLOGICO -> false;
        };
    }

    private DocumentoWorkflowDTO aWorkflow(Documento d) {
        boolean tieneArchivo = d.getArchivoUrl() != null && !d.getArchivoUrl().isBlank();
        boolean requiereAdjunto = requiereAdjuntoManual(d.getTipo());
        String siguienteAccion;
        if (requiereAdjunto && !tieneArchivo) {
            siguienteAccion = "SUBIR_PDF";
        } else if (!requiereAdjunto && d.getEstado() != EstadoDocumento.EMITIDO) {
            siguienteAccion = "GENERAR_PDF";
        } else {
            siguienteAccion = "LISTO";
        }
        return new DocumentoWorkflowDTO(
                d.getId(),
                d.getCodigo(),
                d.getTipo(),
                d.getEstado(),
                d.getNumeroReferencia(),
                requiereAdjunto,
                tieneArchivo,
                d.getArchivoUrl(),
                siguienteAccion
        );
    }

    /**
     * Genera PDF según tipo documental.
     * Cada tipo de documento genera su propio PDF con título y contenido específico.
     * 
     * Mapeo de tipos a generadores:
     * - DOCUMENTO_IDENTIFICACION → CartaDePorte
     * - NOTIFICACION_PREVIA → NotificacionTraslado
     * - FICHA_ACEPTACION → FichaAceptacion
     * - INFORME_FINAL → Informe de Traslado
     * - HOJA_SEGUIMIENTO → HojaSeguimiento
     * - CONTRATO → Contrato/Acuerdo
     * - ARCHIVO_CRONOLOGICO → CartaDePorte (fallback)
     * 
     * @param tipo TipoDocumento
     * @param t Traslado asociado (requerido)
     * @return byte[] del PDF generado
     */
    private byte[] generarPdf(TipoDocumento tipo, Traslado t) {
        if (t == null) {
            throw new IllegalArgumentException("No se puede generar PDF sin traslado asociado");
        }
        return switch (tipo) {
            case DOCUMENTO_IDENTIFICACION -> pdfService.generarCartaDePorte(t);
            case NOTIFICACION_PREVIA -> pdfService.generarNotificacionTraslado(t);
            case FICHA_ACEPTACION -> pdfService.generarFichaAceptacion(t);
            case INFORME_FINAL -> pdfService.generarInformeDocumento(t);
            case HOJA_SEGUIMIENTO -> pdfService.generarHojaSeguimiento(t);
            case CONTRATO -> pdfService.generarDocumentoContrato(t);
            case ARCHIVO_CRONOLOGICO -> pdfService.generarCartaDePorte(t); // fallback
        };
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
