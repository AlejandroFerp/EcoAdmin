package com.iesdoctorbalmis.spring.controladores;

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
import org.springframework.web.bind.annotation.RestController;

import com.iesdoctorbalmis.spring.dto.UsuarioDTO;
import com.iesdoctorbalmis.spring.excepciones.RecursoNoEncontradoException;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.servicios.UsuarioService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Usuarios", description = "Gestion de usuarios del sistema (solo ADMIN)")
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<UsuarioDTO> listar() {
        return service.findAll()
                    .stream()
                    .map(UsuarioDTO::from)
                    .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> buscar(@PathVariable Long id) {
        Usuario u = service.findById(id);
        if (u == null) throw new RecursoNoEncontradoException("Usuario no encontrado: " + id);
        return ResponseEntity.ok(UsuarioDTO.from(u));
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
