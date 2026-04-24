package com.iesdoctorbalmis.spring.controladores;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import com.iesdoctorbalmis.spring.excepciones.AccesoDenegadoException;
import com.iesdoctorbalmis.spring.excepciones.RecursoNoEncontradoException;
import com.iesdoctorbalmis.spring.modelo.EventoTraslado;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;
import com.iesdoctorbalmis.spring.servicios.EmailService;
import com.iesdoctorbalmis.spring.servicios.PdfService;
import com.iesdoctorbalmis.spring.servicios.QrService;
import com.iesdoctorbalmis.spring.servicios.TrasladoService;
import com.iesdoctorbalmis.spring.servicios.UsuarioAutenticadoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Traslados", description = "Gestion de traslados de residuos y maquina de estados")
@RestController
@RequestMapping("/api/traslados")
public class TrasladoController {

    private final TrasladoService service;
    private final PdfService pdfService;
    private final QrService qrService;
    private final UsuarioAutenticadoService authService;
    private final EmailService emailService;

    public TrasladoController(TrasladoService service, PdfService pdfService,
                              QrService qrService, UsuarioAutenticadoService authService,
                              EmailService emailService) {
        this.service = service;
        this.pdfService = pdfService;
        this.qrService = qrService;
        this.authService = authService;
        this.emailService = emailService;
    }

    @Operation(summary = "Listar traslados", description = "Devuelve traslados segun el rol del usuario autenticado")
    @GetMapping
    public List<Traslado> listar() {
        Usuario usuario = authService.obtenerUsuarioActual();
        if (usuario == null) return List.of();

        return switch (usuario.getRol()) {
            case ADMIN, GESTOR -> service.findAll();
            case PRODUCTOR -> service.findByUsuario(usuario);
            case TRANSPORTISTA -> service.findByTransportista(usuario);
        };
    }

    @GetMapping("/{id}")
    public ResponseEntity<Traslado> buscar(@PathVariable Long id) {
        Traslado t = service.findById(id);
        if (t == null) throw new RecursoNoEncontradoException("Traslado no encontrado: " + id);
        verificarAccesoTraslado(t);
        return ResponseEntity.ok(t);
    }

    @GetMapping("/por-estado/{estado}")
    public List<Traslado> porEstado(@PathVariable EstadoTraslado estado) {
        return service.findByEstado(estado);
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<EventoTraslado>> historial(@PathVariable Long id) {
        Traslado t = service.findById(id);
        if (t == null) throw new RecursoNoEncontradoException("Traslado no encontrado: " + id);
        verificarAccesoTraslado(t);
        List<EventoTraslado> eventos = service.historialDeTraslado(id);
        return ResponseEntity.ok(eventos);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'PRODUCTOR')")
    public ResponseEntity<Traslado> crear(@RequestBody Traslado t) {
        Traslado saved = service.save(t);
        emailService.notificarNuevoTraslado(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<Traslado> editar(@PathVariable Long id, @RequestBody Traslado t) {
        Traslado existing = service.findById(id);
        if (existing == null) throw new RecursoNoEncontradoException("Traslado no encontrado: " + id);
        verificarAccesoTraslado(existing);
        t.setId(id);
        t.setEstado(existing.getEstado());
        t.setFechaInicioTransporte(existing.getFechaInicioTransporte());
        t.setFechaEntrega(existing.getFechaEntrega());
        return ResponseEntity.ok(service.save(t));
    }

    @Operation(summary = "Cambiar estado de traslado",
               description = "Permite transiciones libres entre estados; el historial registra cada cambio")
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'TRANSPORTISTA')")
    public ResponseEntity<Traslado> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoTraslado estado,
            @RequestParam(required = false) String comentario) {

        Usuario usuario = authService.obtenerUsuarioActual();
        Traslado traslado = service.findById(id);
        if (traslado == null) throw new RecursoNoEncontradoException("Traslado no encontrado: " + id);
        verificarAccesoTraslado(traslado);

        Traslado actualizado = service.cambiarEstado(id, estado, comentario, usuario);
        emailService.notificarCambioEstado(actualizado);

        // Al completar el traslado, enviar el certificado de recepcion al productor
        if (actualizado.getEstado() == EstadoTraslado.COMPLETADO) {
            enviarCertificadoAlProductor(actualizado);
        }
        return ResponseEntity.ok(actualizado);
    }

    @Operation(summary = "Asignar o desasignar ruta a un traslado")
    @PatchMapping("/{id}/ruta")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<Traslado> asignarRuta(@PathVariable Long id,
                                                 @RequestParam(required = false) Long rutaId) {
        Traslado resultado = service.asignarRuta(id, rutaId);
        return ResponseEntity.ok(resultado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Traslado t = service.findById(id);
        if (t == null) throw new RecursoNoEncontradoException("Traslado no encontrado: " + id);
        verificarAccesoTraslado(t);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/qr")
    public ResponseEntity<byte[]> generarQr(@PathVariable Long id, HttpServletRequest request) {
        Traslado traslado = service.findById(id);
        if (traslado == null) throw new RecursoNoEncontradoException("Traslado no encontrado: " + id);
        verificarAccesoTraslado(traslado);

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
    public ResponseEntity<byte[]> generarPdf(@PathVariable Long id, @PathVariable String tipo,
                                              @RequestParam(defaultValue = "false") boolean inline) {
        Traslado traslado = service.findById(id);
        if (traslado == null) throw new RecursoNoEncontradoException("Traslado no encontrado: " + id);
        verificarAccesoTraslado(traslado);

        byte[] pdf = switch (tipo.toLowerCase()) {
            case "carta-porte"   -> pdfService.generarCartaDePorte(traslado);
            case "notificacion"  -> pdfService.generarNotificacionTraslado(traslado);
            case "certificado"   -> pdfService.generarCertificadoRecepcion(traslado);
            default -> null;
        };

        if (pdf == null) return ResponseEntity.badRequest().build();

        String disposition = inline
                ? "inline; filename=\"traslado-" + id + "-" + tipo + ".pdf\""
                : "attachment; filename=\"traslado-" + id + "-" + tipo + ".pdf\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // -------------------------------------------------------------------------

    private void enviarCertificadoAlProductor(Traslado traslado) {
        if (traslado.getCentroProductor() == null
                || traslado.getCentroProductor().getUsuario() == null
                || traslado.getCentroProductor().getUsuario().getEmail() == null
                || traslado.getCentroProductor().getUsuario().getEmail().isBlank()) {
            return;
        }
        byte[] certificado = pdfService.generarCertificadoRecepcion(traslado);
        String emailProductor = traslado.getCentroProductor().getUsuario().getEmail();
        emailService.enviarCertificado(traslado, emailProductor, certificado);
    }

    private void verificarAccesoTraslado(Traslado traslado) {
        Usuario usuario = authService.obtenerUsuarioActual();
        if (usuario == null) throw new AccesoDenegadoException("No autenticado");
        if (authService.esAdmin(usuario)) return;

        Rol rol = usuario.getRol();
        boolean tieneAcceso = switch (rol) {
            case GESTOR -> true;
            case PRODUCTOR -> traslado.getCentroProductor() != null
                    && traslado.getCentroProductor().getUsuario() != null
                    && traslado.getCentroProductor().getUsuario().getId().equals(usuario.getId());
            case TRANSPORTISTA -> traslado.getTransportista() != null
                    && traslado.getTransportista().getId().equals(usuario.getId());
            default -> false;
        };

        if (!tieneAcceso) {
            throw new AccesoDenegadoException("No tiene acceso a este traslado");
        }
    }
}
