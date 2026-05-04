package com.alejandrofernandez.ecoadmin.controladores;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;

import com.alejandrofernandez.ecoadmin.dto.PerfilEdicionDTO;
import com.alejandrofernandez.ecoadmin.dto.ResetPasswordDTO;
import com.alejandrofernandez.ecoadmin.dto.UsuarioDTO;
import com.alejandrofernandez.ecoadmin.excepciones.RecursoNoEncontradoException;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.repository.UsuarioRepository;
import com.alejandrofernandez.ecoadmin.servicios.UsuarioService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Usuarios", description = "Gestion de usuarios del sistema (solo ADMIN)")
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService service;
    private final UsuarioRepository usuarioRepo;

    public UsuarioController(UsuarioService service, UsuarioRepository usuarioRepo) {
        this.service = service;
        this.usuarioRepo = usuarioRepo;
    }

    @GetMapping
    public List<UsuarioDTO> listar(@RequestParam(required = false) Rol rol) {
        var lista = (rol != null)
                ? usuarioRepo.findByRol(rol)
                : service.findAll();
        return lista.stream()
                    .map(UsuarioDTO::from)
                    .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> buscar(@PathVariable Long id) {
        Usuario u = service.findById(id);
        if (u == null) throw new RecursoNoEncontradoException("Usuario no encontrado: " + id);
        return ResponseEntity.ok(UsuarioDTO.from(u));
    }

    @GetMapping("/{id}/perfil")
    public ResponseEntity<UsuarioDTO> obtenerPerfil(@PathVariable Long id) {
        Usuario u = service.findById(id);
        if (u == null) throw new RecursoNoEncontradoException("Usuario no encontrado: " + id);
        return ResponseEntity.ok(UsuarioDTO.from(u));
    }

    @PutMapping("/{id}/perfil")
    public ResponseEntity<?> editarPerfil(@PathVariable Long id, @RequestBody PerfilEdicionDTO datos) {
        Usuario u = service.findById(id);
        if (u == null) throw new RecursoNoEncontradoException("Usuario no encontrado: " + id);

        if (datos.nombre() == null || datos.nombre().isBlank()) {
            return ResponseEntity.badRequest().body("El nombre es obligatorio.");
        }
        if (datos.email() == null || datos.email().isBlank()) {
            return ResponseEntity.badRequest().body("El email es obligatorio.");
        }

        var existente = usuarioRepo.findByEmail(datos.email().trim()).orElse(null);
        if (existente != null && !existente.getId().equals(u.getId())) {
            return ResponseEntity.badRequest().body("Ya existe otro usuario con ese email.");
        }

        u.setNombre(datos.nombre().trim());
        u.setEmail(datos.email().trim());
        u.setTelefono(trimOrNull(datos.telefono()));
        u.setDni(trimOrNull(datos.dni()));
        u.setCargo(trimOrNull(datos.cargo()));
        u.setFotoUrl(trimOrNull(datos.fotoUrl()));
        if (datos.notificacionesEmail() != null) {
            u.setNotificacionesEmail(datos.notificacionesEmail());
        }

        Usuario saved = service.save(u);
        return ResponseEntity.ok(UsuarioDTO.from(saved));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody ResetPasswordDTO datos) {
        Usuario u = service.findById(id);
        if (u == null) throw new RecursoNoEncontradoException("Usuario no encontrado: " + id);
        if (datos.passwordNueva() == null || datos.passwordNueva().length() < 6) {
            return ResponseEntity.badRequest().body("La nueva contrasena debe tener al menos 6 caracteres.");
        }
        u.setPassword(datos.passwordNueva());
        service.save(u);
        return ResponseEntity.noContent().build();
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Usuario u) {
        if (u.getPassword() == null || u.getPassword().isBlank())
            return ResponseEntity.badRequest().body("La contrasena es obligatoria al crear un usuario.");
        if (u.getEmail() == null || u.getEmail().isBlank())
            return ResponseEntity.badRequest().body("El email es obligatorio.");
        try {
            Usuario saved = service.save(u);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(UsuarioDTO.from(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear usuario: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id, @RequestBody Usuario u) {
        Usuario existing = service.findById(id);
        if (existing == null) throw new RecursoNoEncontradoException("Usuario no encontrado: " + id);
        existing.setNombre(u.getNombre());
        existing.setEmail(u.getEmail());
        existing.setRol(u.getRol());
        // La password NO se modifica desde este endpoint (evita perder/re-cifrar el hash al editar otros campos).
        try {
            Usuario saved = service.save(existing);
            return ResponseEntity.ok(UsuarioDTO.from(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar usuario: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (service.findById(id) == null) throw new RecursoNoEncontradoException("Usuario no encontrado: " + id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
