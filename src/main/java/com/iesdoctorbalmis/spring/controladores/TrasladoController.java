package com.iesdoctorbalmis.spring.controladores;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import com.iesdoctorbalmis.spring.modelo.EventoTraslado;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;
import com.iesdoctorbalmis.spring.servicios.PdfService;
import com.iesdoctorbalmis.spring.servicios.QrService;
import com.iesdoctorbalmis.spring.servicios.TrasladoService;

@RestController
@RequestMapping("/api/traslados")
public class TrasladoController {

    @Autowired
    private TrasladoService service;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private QrService qrService;

    @GetMapping
    public List<Traslado> listar() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Traslado> buscar(@PathVariable Long id) {
        Traslado t = service.findById(id);
        return t != null ? ResponseEntity.ok(t) : ResponseEntity.notFound().build();
    }

    @GetMapping("/por-estado/{estado}")
    public List<Traslado> porEstado(@PathVariable EstadoTraslado estado) {
        return service.findByEstado(estado);
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<EventoTraslado>> historial(@PathVariable Long id) {
        List<EventoTraslado> eventos = service.historialDeTraslado(id);
        return ResponseEntity.ok(eventos);
    }

    @PostMapping
    public Traslado crear(@RequestBody Traslado t) {
        return service.save(t);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Traslado> editar(@PathVariable Long id, @RequestBody Traslado t) {
        if (service.findById(id) == null) return ResponseEntity.notFound().build();
        t.setId(id);
        return ResponseEntity.ok(service.save(t));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Traslado> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoTraslado estado,
            @RequestParam(required = false) String comentario,
            Authentication auth) {

        Usuario usuario = auth != null
                ? usuarioRepo.findByEmail(auth.getName()).orElse(null)
                : null;

        Traslado actualizado = service.cambiarEstado(id, estado, comentario, usuario);
        return actualizado != null ? ResponseEntity.ok(actualizado) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (service.findById(id) == null) return ResponseEntity.notFound().build();
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/qr")
    public ResponseEntity<byte[]> generarQr(@PathVariable Long id, HttpServletRequest request) {
        Traslado traslado = service.findById(id);
        if (traslado == null) return ResponseEntity.notFound().build();

        String baseUrl = request.getScheme() + "://" + request.getServerName()
                + ":" + request.getServerPort();
        byte[] qr = qrService.generarQrTraslado(id, baseUrl);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"traslado-" + id + "-qr.png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(qr);
    }

    @GetMapping("/{id}/pdf/{tipo}")
    public ResponseEntity<byte[]> generarPdf(@PathVariable Long id, @PathVariable String tipo) {
        Traslado traslado = service.findById(id);
        if (traslado == null) return ResponseEntity.notFound().build();

        byte[] pdf = switch (tipo.toLowerCase()) {
            case "carta-porte"   -> pdfService.generarCartaDePorte(traslado);
            case "notificacion"  -> pdfService.generarNotificacionTraslado(traslado);
            case "certificado"   -> pdfService.generarCertificadoRecepcion(traslado);
            default -> null;
        };

        if (pdf == null) return ResponseEntity.badRequest().build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"traslado-" + id + "-" + tipo + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
