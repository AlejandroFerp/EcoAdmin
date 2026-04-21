package com.iesdoctorbalmis.spring.controladores;

import java.util.List;
import java.util.stream.Collectors;

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

import com.iesdoctorbalmis.spring.excepciones.RecursoNoEncontradoException;
import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Direccion;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.servicios.DireccionService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Direcciones", description = "Gestion de direcciones fisicas de los centros")
@RestController
@RequestMapping("/api/direcciones")
public class DireccionController {

    private final DireccionService service;
    private final CentroRepository centroRepo;

    public DireccionController(DireccionService service, CentroRepository centroRepo) {
        this.service = service;
        this.centroRepo = centroRepo;
    }

    @GetMapping
    public List<Direccion> listar() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Direccion> buscar(@PathVariable Long id) {
        Direccion d = service.findById(id);
        if (d == null) throw new RecursoNoEncontradoException("Direccion no encontrada: " + id);
        return ResponseEntity.ok(d);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'PRODUCTOR')")
    public ResponseEntity<Direccion> crear(@RequestBody Direccion d) {
        Direccion saved = service.save(d);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'PRODUCTOR')")
    public ResponseEntity<Direccion> editar(@PathVariable Long id, @RequestBody Direccion d) {
        if (service.findById(id) == null) throw new RecursoNoEncontradoException("Direccion no encontrada: " + id);
        d.setId(id);
        return ResponseEntity.ok(service.save(d));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        Direccion d = service.findById(id);
        if (d == null) throw new RecursoNoEncontradoException("Direccion no encontrada: " + id);

        List<Centro> dependientes = centroRepo.findByDireccion(d);
        if (!dependientes.isEmpty()) {
            String nombres = dependientes.stream()
                .map(Centro::getNombre)
                .collect(Collectors.joining(", "));
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(java.util.Map.of("error",
                    "No se puede eliminar: los centros [" + nombres + "] dependen de esta direccion."));
        }

        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}