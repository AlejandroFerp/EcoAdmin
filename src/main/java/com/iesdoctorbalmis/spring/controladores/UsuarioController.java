package com.iesdoctorbalmis.spring.controladores;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.servicios.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    @GetMapping
    public List<UsuarioDTO> listar() {
        return service.findAll()
                    .stream()
                    .map(u -> new UsuarioDTO(u.getId(), u.getNombre(), u.getEmail(), u.getRol(), u.getFechaAlta()))
                    .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> buscar(@PathVariable Long id) {
        Usuario u = service.findById(id);
        if (u == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new UsuarioDTO(u.getId(), u.getNombre(), u.getEmail(), u.getRol(), u.getFechaAlta()));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Usuario u) {
        if (u.getPassword() == null || u.getPassword().isBlank())
            return ResponseEntity.badRequest().body("La contraseña es obligatoria al crear un usuario.");
        if (u.getEmail() == null || u.getEmail().isBlank())
            return ResponseEntity.badRequest().body("El email es obligatorio.");
        try {
            Usuario saved = service.save(u);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new UsuarioDTO(saved.getId(), saved.getNombre(), saved.getEmail(), saved.getRol(), saved.getFechaAlta()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear usuario: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id, @RequestBody Usuario u) {
        Usuario existing = service.findById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        existing.setNombre(u.getNombre());
        existing.setEmail(u.getEmail());
        existing.setRol(u.getRol());
        // Only update password if a new one is provided
        if (u.getPassword() != null && !u.getPassword().isBlank()) {
            existing.setPassword(u.getPassword());
        }
        try {
            Usuario saved = service.save(existing);
            return ResponseEntity.ok(new UsuarioDTO(saved.getId(), saved.getNombre(), saved.getEmail(), saved.getRol(), saved.getFechaAlta()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar usuario: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (service.findById(id) == null) return ResponseEntity.notFound().build();
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
