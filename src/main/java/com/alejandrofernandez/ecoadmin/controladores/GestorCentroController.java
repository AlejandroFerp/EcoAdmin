package com.alejandrofernandez.ecoadmin.controladores;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alejandrofernandez.ecoadmin.excepciones.RecursoNoEncontradoException;
import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.GestorCentro;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;
import com.alejandrofernandez.ecoadmin.repository.CentroRepository;
import com.alejandrofernandez.ecoadmin.repository.GestorCentroRepository;
import com.alejandrofernandez.ecoadmin.repository.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Asignaciones Gestor-Centro", description = "Gestion de asignaciones entre gestores y centros (solo Admin)")
@RestController
@RequestMapping("/api/gestor-centros")
@PreAuthorize("hasRole('ADMIN')")
public class GestorCentroController {

    private final GestorCentroRepository gestorCentroRepo;
    private final UsuarioRepository usuarioRepo;
    private final CentroRepository centroRepo;

    public GestorCentroController(GestorCentroRepository gestorCentroRepo,
                                  UsuarioRepository usuarioRepo,
                                  CentroRepository centroRepo) {
        this.gestorCentroRepo = gestorCentroRepo;
        this.usuarioRepo = usuarioRepo;
        this.centroRepo = centroRepo;
    }

    @Operation(summary = "Listar todas las asignaciones gestor-centro")
    @GetMapping
    public List<Map<String, Object>> listar() {
        return gestorCentroRepo.findAll().stream().map(this::toDto).toList();
    }

    @Operation(summary = "Asignaciones de un gestor concreto")
    @GetMapping("/por-gestor/{gestorId}")
    public List<Map<String, Object>> porGestor(@PathVariable Long gestorId) {
        Usuario gestor = usuarioRepo.findById(gestorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + gestorId));
        return gestorCentroRepo.findByGestor(gestor).stream().map(this::toDto).toList();
    }

    @Operation(summary = "Gestores asignados a un centro concreto")
    @GetMapping("/por-centro/{centroId}")
    public List<Map<String, Object>> porCentro(@PathVariable Long centroId) {
        Centro centro = centroRepo.findById(centroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Centro no encontrado: " + centroId));
        return gestorCentroRepo.findByCentro(centro).stream().map(this::toDto).toList();
    }

    @Operation(summary = "Asignar un centro a un gestor")
    @PostMapping
    public ResponseEntity<?> asignar(@RequestBody AsignacionInput input) {
        Usuario gestor = usuarioRepo.findById(input.gestorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + input.gestorId()));
        if (gestor.getRol() != Rol.GESTOR) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El usuario " + gestor.getNombre() + " no tiene rol GESTOR"));
        }

        Centro centro = centroRepo.findById(input.centroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Centro no encontrado: " + input.centroId()));

        if (gestorCentroRepo.existsByGestorAndCentro(gestor, centro)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El gestor ya tiene asignado este centro"));
        }

        GestorCentro gc = new GestorCentro(gestor, centro);
        gestorCentroRepo.save(gc);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(gc));
    }

    @Operation(summary = "Desasignar un centro de un gestor")
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> desasignar(@PathVariable Long id) {
        if (!gestorCentroRepo.existsById(id)) {
            throw new RecursoNoEncontradoException("Asignacion no encontrada: " + id);
        }
        gestorCentroRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> toDto(GestorCentro gc) {
        return Map.of(
                "id", gc.getId(),
                "gestorId", gc.getGestor().getId(),
                "gestorNombre", gc.getGestor().getNombre(),
                "centroId", gc.getCentro().getId(),
                "centroNombre", gc.getCentro().getNombre(),
                "fechaAsignacion", gc.getFechaAsignacion() != null ? gc.getFechaAsignacion().toString() : "");
    }

    public record AsignacionInput(Long gestorId, Long centroId) {}
}
