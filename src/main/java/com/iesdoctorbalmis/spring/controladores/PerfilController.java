package com.iesdoctorbalmis.spring.controladores;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iesdoctorbalmis.spring.dto.CambioPasswordDTO;
import com.iesdoctorbalmis.spring.dto.PerfilEdicionDTO;
import com.iesdoctorbalmis.spring.dto.UsuarioDTO;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;
import com.iesdoctorbalmis.spring.servicios.UsuarioAutenticadoService;
import com.iesdoctorbalmis.spring.servicios.UsuarioService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Perfil", description = "Perfil del usuario autenticado (Mis Datos)")
@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final UsuarioAutenticadoService usuarioActual;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder encoder;

    public PerfilController(UsuarioAutenticadoService usuarioActual,
                            UsuarioService usuarioService,
                            UsuarioRepository usuarioRepo,
                            PasswordEncoder encoder) {
        this.usuarioActual = usuarioActual;
        this.usuarioService = usuarioService;
        this.usuarioRepo = usuarioRepo;
        this.encoder = encoder;
    }

    @GetMapping
    public ResponseEntity<?> obtener() {
        Usuario u = usuarioActual.obtenerUsuarioActual();
        if (u == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(UsuarioDTO.from(u));
    }

    @PutMapping
    public ResponseEntity<?> editar(@RequestBody PerfilEdicionDTO datos) {
        Usuario u = usuarioActual.obtenerUsuarioActual();
        if (u == null) return ResponseEntity.status(401).build();
        if (datos.nombre() == null || datos.nombre().isBlank())
            return ResponseEntity.badRequest().body("El nombre es obligatorio.");
        if (datos.email() == null || datos.email().isBlank())
            return ResponseEntity.badRequest().body("El email es obligatorio.");

        // Comprobar que el email no esta usado por otro usuario
        var existente = usuarioRepo.findByEmail(datos.email().trim()).orElse(null);
        if (existente != null && !existente.getId().equals(u.getId()))
            return ResponseEntity.badRequest().body("Ya existe otro usuario con ese email.");

        u.setNombre(datos.nombre().trim());
        u.setEmail(datos.email().trim());
        u.setTelefono(trimOrNull(datos.telefono()));
        u.setDni(trimOrNull(datos.dni()));
        u.setCargo(trimOrNull(datos.cargo()));
        u.setFotoUrl(trimOrNull(datos.fotoUrl()));
        if (datos.notificacionesEmail() != null) {
            u.setNotificacionesEmail(datos.notificacionesEmail());
        }
        Usuario saved = usuarioService.save(u);
        return ResponseEntity.ok(UsuarioDTO.from(saved));
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @PutMapping("/password")
    public ResponseEntity<?> cambiarPassword(@RequestBody CambioPasswordDTO datos) {
        Usuario u = usuarioActual.obtenerUsuarioActual();
        if (u == null) return ResponseEntity.status(401).build();
        if (datos.passwordActual() == null || datos.passwordActual().isBlank())
            return ResponseEntity.badRequest().body("Debes indicar tu contrasena actual.");
        if (datos.passwordNueva() == null || datos.passwordNueva().length() < 6)
            return ResponseEntity.badRequest().body("La nueva contrasena debe tener al menos 6 caracteres.");
        if (!encoder.matches(datos.passwordActual(), u.getPassword()))
            return ResponseEntity.badRequest().body("La contrasena actual no es correcta.");

        u.setPassword(datos.passwordNueva()); // UsuarioServiceDB.save() la cifrara
        usuarioService.save(u);
        return ResponseEntity.noContent().build();
    }
}
