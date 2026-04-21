package com.iesdoctorbalmis.spring.controladores;

import java.util.List;

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

import com.iesdoctorbalmis.spring.excepciones.RecursoNoEncontradoException;
import com.iesdoctorbalmis.spring.modelo.Documento;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.enums.TipoDocumento;
import com.iesdoctorbalmis.spring.servicios.DocumentoService;
import com.iesdoctorbalmis.spring.servicios.PdfService;
import com.iesdoctorbalmis.spring.servicios.TrasladoService;

@RestController
@RequestMapping("/api/documentos")
public class DocumentoController {

    private final DocumentoService service;
    private final TrasladoService trasladoService;
    private final PdfService pdfService;

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
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(d));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Documento> editar(@PathVariable Long id, @RequestBody Documento d) {
        if (service.findById(id) == null)
            throw new RecursoNoEncontradoException("Documento no encontrado: " + id);
        d.setId(id);
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
}
