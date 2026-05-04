package com.alejandrofernandez.ecoadmin.controladores;

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

import com.alejandrofernandez.ecoadmin.dto.TrasladoDTO;
import com.alejandrofernandez.ecoadmin.dto.TrasladoInputDTO;
import com.alejandrofernandez.ecoadmin.excepciones.AccesoDenegadoException;
import com.alejandrofernandez.ecoadmin.excepciones.RecursoNoEncontradoException;
import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.EventoTraslado;
import com.alejandrofernandez.ecoadmin.modelo.Residuo;
import com.alejandrofernandez.ecoadmin.modelo.Traslado;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoTraslado;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;
import com.alejandrofernandez.ecoadmin.servicios.CentroService;
import com.alejandrofernandez.ecoadmin.servicios.EmailService;
import com.alejandrofernandez.ecoadmin.servicios.OwnershipService;
import com.alejandrofernandez.ecoadmin.servicios.PdfService;
import com.alejandrofernandez.ecoadmin.servicios.QrService;
import com.alejandrofernandez.ecoadmin.servicios.ResiduoService;
import com.alejandrofernandez.ecoadmin.servicios.TrasladoService;
import com.alejandrofernandez.ecoadmin.servicios.UsuarioAutenticadoService;
import com.alejandrofernandez.ecoadmin.servicios.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Traslados", description = "Gestion de traslados de residuos y maquina de estados")
@RestController
@RequestMapping("/api/traslados")
public class TrasladoController {

    private final TrasladoService service;
    private final CentroService centroService;
    private final ResiduoService residuoService;
    private final UsuarioService usuarioService;
    private final PdfService pdfService;
    private final QrService qrService;
    private final UsuarioAutenticadoService authService;
    private final EmailService emailService;
    private final OwnershipService ownershipService;

    public TrasladoController(TrasladoService service, CentroService centroService,
                              ResiduoService residuoService, UsuarioService usuarioService,
                              PdfService pdfService, QrService qrService,
                              UsuarioAutenticadoService authService, EmailService emailService,
                              OwnershipService ownershipService) {
        this.service = service;
        this.centroService = centroService;
        this.residuoService = residuoService;
        this.usuarioService = usuarioService;
        this.pdfService = pdfService;
        this.qrService = qrService;
        this.authService = authService;
        this.emailService = emailService;
        this.ownershipService = ownershipService;
    }

    @Operation(summary = "Listar traslados", description = "Devuelve traslados segun el rol del usuario autenticado")
    @GetMapping
    public List<TrasladoDTO> listar() {
        Usuario usuario = authService.obtenerUsuarioActual();
        if (usuario == null) return List.of();
        return service.findAllForUsuario(usuario).stream().map(TrasladoDTO::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrasladoDTO> buscar(@PathVariable Long id) {
        Traslado t = service.findById(id);
        if (t == null) throw new RecursoNoEncontradoException("Traslado no encontrado: " + id);
        verificarAccesoTraslado(t);
        return ResponseEntity.ok(TrasladoDTO.from(t));
    }

    @GetMapping("/por-estado/{estado}")
    public List<TrasladoDTO> porEstado(@PathVariable EstadoTraslado estado) {
        Usuario usuario = authService.obtenerUsuarioActual();
        if (usuario == null) return List.of();
        List<Traslado> todos = service.findByEstado(estado);
        List<Traslado> filtrados = switch (usuario.getRol()) {
            case ADMIN, GESTOR -> todos;
            case PRODUCTOR -> todos.stream()
                .filter(t -> t.getCentroProductor() != null && t.getCentroProductor().getUsuario() != null
                        && t.getCentroProductor().getUsuario().getId().equals(usuario.getId()))
                .toList();
            case TRANSPORTISTA -> todos.stream()
                .filter(t -> t.getTransportista() != null
                        && t.getTransportista().getId().equals(usuario.getId()))
                .toList();
        };
        return filtrados.stream().map(TrasladoDTO::from).toList();
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
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<TrasladoDTO> crear(@RequestBody TrasladoInputDTO input) {
        Usuario usuario = authService.obtenerUsuarioActual();
        if (usuario.getRol() == Rol.GESTOR
                && !ownershipService.canAccessCentro(usuario, input.centroProductorId())) {
            throw new AccesoDenegadoException("No tiene acceso al centro productor indicado");
        }
        Traslado t = mapInputToEntity(input);
        t.setEstado(EstadoTraslado.PENDIENTE);
        Traslado saved = service.save(t);
        emailService.notificarNuevoTraslado(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(TrasladoDTO.from(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<TrasladoDTO> editar(@PathVariable Long id, @RequestBody TrasladoInputDTO input) {
        Traslado existing = service.findById(id);
        if (existing == null) throw new RecursoNoEncontradoException("Traslado no encontrado: " + id);
        verificarAccesoTraslado(existing);

        Traslado t = mapInputToEntity(input);
        t.setId(id);
        t.setEstado(existing.getEstado());
        t.setFechaInicioTransporte(existing.getFechaInicioTransporte());
        t.setFechaEntrega(existing.getFechaEntrega());
        return ResponseEntity.ok(TrasladoDTO.from(service.save(t)));
    }

    @Operation(summary = "Cambiar estado de traslado",
               description = "Permite transiciones libres entre estados; el historial registra cada cambio")
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'TRANSPORTISTA')")
    public ResponseEntity<TrasladoDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoTraslado estado,
            @RequestParam(required = false) String comentario) {

        Usuario usuario = authService.obtenerUsuarioActual();
        Traslado traslado = service.findById(id);
        if (traslado == null) throw new RecursoNoEncontradoException("Traslado no encontrado: " + id);
        verificarAccesoTraslado(traslado);

        Traslado actualizado = service.cambiarEstado(id, estado, comentario, usuario);
        emailService.notificarCambioEstado(actualizado);

        if (actualizado.getEstado() == EstadoTraslado.COMPLETADO) {
            enviarCertificadoAlProductor(actualizado);
        }
        return ResponseEntity.ok(TrasladoDTO.from(actualizado));
    }

    @Operation(summary = "Asignar o desasignar ruta a un traslado")
    @PatchMapping("/{id}/ruta")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<TrasladoDTO> asignarRuta(@PathVariable Long id,
                                                 @RequestParam(required = false) Long rutaId) {
        Traslado resultado = service.asignarRuta(id, rutaId);
        return ResponseEntity.ok(TrasladoDTO.from(resultado));
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

    private Traslado mapInputToEntity(TrasladoInputDTO input) {
        Centro cp = centroService.findById(input.centroProductorId());
        if (cp == null) throw new RecursoNoEncontradoException("Centro productor no encontrado: " + input.centroProductorId());

        Centro cg = centroService.findById(input.centroGestorId());
        if (cg == null) throw new RecursoNoEncontradoException("Centro gestor no encontrado: " + input.centroGestorId());

        Residuo residuo = residuoService.findById(input.residuoId());
        if (residuo == null) throw new RecursoNoEncontradoException("Residuo no encontrado: " + input.residuoId());

        Usuario transportista = null;
        if (input.transportistaId() != null) {
            transportista = usuarioService.findById(input.transportistaId());
            if (transportista == null) throw new RecursoNoEncontradoException("Transportista no encontrado: " + input.transportistaId());
        }

        Traslado t = new Traslado(cp, cg, residuo, transportista);
        t.setObservaciones(input.observaciones());
        t.setFechaProgramadaInicio(input.fechaProgramadaInicio());
        t.setFechaProgramadaFin(input.fechaProgramadaFin());
        return t;
    }

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
            case GESTOR -> (traslado.getCentroProductor() != null
                    && ownershipService.canAccessCentro(usuario, traslado.getCentroProductor().getId()))
                    || (traslado.getCentroGestor() != null
                    && ownershipService.canAccessCentro(usuario, traslado.getCentroGestor().getId()));
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
