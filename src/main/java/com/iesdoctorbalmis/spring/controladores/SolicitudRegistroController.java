package com.iesdoctorbalmis.spring.controladores;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.iesdoctorbalmis.spring.modelo.SolicitudRegistro;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoSolicitud;
import com.iesdoctorbalmis.spring.servicios.SolicitudRegistroService;
import com.iesdoctorbalmis.spring.servicios.UsuarioAutenticadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/solicitudes-registro")
@Tag(name = "Solicitudes de Registro", description = "Registro publico y gestion admin de solicitudes")
public class SolicitudRegistroController {

    private final SolicitudRegistroService service;
    private final UsuarioAutenticadoService authService;

    public SolicitudRegistroController(SolicitudRegistroService service,
                                        UsuarioAutenticadoService authService) {
        this.service = service;
        this.authService = authService;
    }

    @PostMapping
    @Operation(summary = "Crear solicitud de registro (publico)")
    public ResponseEntity<SolicitudRegistro> crear(@RequestBody SolicitudRegistro solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(solicitud));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar solicitudes (ADMIN)")
    public List<SolicitudRegistro> listar(@RequestParam(required = false) EstadoSolicitud estado) {
        if (estado != null) return service.listarPorEstado(estado);
        return service.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Detalle de solicitud (ADMIN)")
    public SolicitudRegistro detalle(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aprobar solicitud y crear usuario (ADMIN)")
    public ResponseEntity<Void> aprobar(@PathVariable Long id,
                                         @RequestBody Map<String, String> body) {
        String password = body.get("password");
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("La contrasena debe tener al menos 6 caracteres");
        }
        Usuario admin = authService.obtenerUsuarioActual();
        service.aprobar(id, password, admin);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rechazar solicitud (ADMIN)")
    public ResponseEntity<SolicitudRegistro> rechazar(@PathVariable Long id,
                                                       @RequestBody Map<String, String> body) {
        String motivo = body.getOrDefault("motivo", "Sin motivo especificado");
        Usuario admin = authService.obtenerUsuarioActual();
        return ResponseEntity.ok(service.rechazar(id, motivo, admin));
    }
}
