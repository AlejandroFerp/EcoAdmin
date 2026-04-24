package com.iesdoctorbalmis.spring.controladores;

import java.util.List;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

import com.iesdoctorbalmis.spring.excepciones.RecursoNoEncontradoException;
import com.iesdoctorbalmis.spring.modelo.Documento;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.enums.TipoDocumento;
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

    @Value("${ecoadmin.uploads.documentos:uploads/documentos}")
    private String directorioUploads;

    public DocumentoController(DocumentoService service, TrasladoService trasladoService,
                               PdfService pdfService) {
        this.service = service;
        this.trasladoService = trasladoService;
        this.pdfService = pdfService;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(d));
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
        return ResponseEntity.ok(service.save(d));
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
}
