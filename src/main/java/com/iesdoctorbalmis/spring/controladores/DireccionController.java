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

import com.iesdoctorbalmis.spring.excepciones.RecursoNoEncontradoException;
import com.iesdoctorbalmis.spring.modelo.Direccion;
import com.iesdoctorbalmis.spring.servicios.DireccionService;

@RestController
@RequestMapping("/api/direcciones")
@PreAuthorize("hasRole('ADMIN')")
public class DireccionController {

    private final DireccionService service;

    public DireccionController(DireccionService service) {
        this.service = service;
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
    public ResponseEntity<Direccion> crear(@RequestBody Direccion d) {
        Direccion saved = service.save(d);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Direccion> editar(@PathVariable Long id, @RequestBody Direccion d) {
        if (service.findById(id) == null) throw new RecursoNoEncontradoException("Direccion no encontrada: " + id);
        d.setId(id);
        return ResponseEntity.ok(service.save(d));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (service.findById(id) == null) throw new RecursoNoEncontradoException("Direccion no encontrada: " + id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
