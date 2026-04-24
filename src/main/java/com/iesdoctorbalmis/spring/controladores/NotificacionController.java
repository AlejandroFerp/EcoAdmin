package com.iesdoctorbalmis.spring.controladores;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.iesdoctorbalmis.spring.modelo.Notificacion;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.servicios.NotificacionService;
import com.iesdoctorbalmis.spring.servicios.UsuarioAutenticadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "Notificaciones", description = "Notificaciones del usuario autenticado")
public class NotificacionController {

    private final NotificacionService service;
    private final UsuarioAutenticadoService authService;

    public NotificacionController(NotificacionService service, UsuarioAutenticadoService authService) {
        this.service = service;
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "Listar mis notificaciones")
    public List<Notificacion> listar() {
        Usuario u = authService.obtenerUsuarioActual();
        return service.listar(u);
    }

    @GetMapping("/no-leidas")
    @Operation(summary = "Contador de notificaciones no leidas")
    public Map<String, Long> noLeidas() {
        Usuario u = authService.obtenerUsuarioActual();
        return Map.of("count", service.contarNoLeidas(u));
    }

    @PatchMapping("/{id}/leer")
    @Operation(summary = "Marcar notificacion como leida")
    public ResponseEntity<Notificacion> marcarLeida(@PathVariable Long id) {
        Usuario u = authService.obtenerUsuarioActual();
        return ResponseEntity.ok(service.marcarLeida(id, u));
    }
}
