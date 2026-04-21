package com.iesdoctorbalmis.spring.controladores;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iesdoctorbalmis.spring.excepciones.RecursoNoEncontradoException;
import com.iesdoctorbalmis.spring.modelo.Documento;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.servicios.DocumentoService;
import com.iesdoctorbalmis.spring.servicios.TrasladoService;

@RestController
@RequestMapping("/api/documentos")
public class DocumentoController {

    private final DocumentoService service;
    private final TrasladoService trasladoService;

    public DocumentoController(DocumentoService service, TrasladoService trasladoService) {
        this.service = service;
        this.trasladoService = trasladoService;
    }

    @GetMapping
    public List<Documento> listar() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Documento> buscar(@PathVariable Long id) {
        Documento d = service.findById(id);
        if (d == null) throw new RecursoNoEncontradoException("Documento no encontrado: " + id);
        return ResponseEntity.ok(d);
    }

    @GetMapping("/traslado/{trasladoId}")
    public List<Documento> porTraslado(@PathVariable Long trasladoId) {
        Traslado t = trasladoService.findById(trasladoId);
        if (t == null) throw new RecursoNoEncontradoException("Traslado no encontrado: " + trasladoId);
        return service.findByTraslado(t);
    }

    @PostMapping
    public ResponseEntity<Documento> crear(@RequestBody Documento d) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(d));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Documento> editar(@PathVariable Long id, @RequestBody Documento d) {
        if (service.findById(id) == null)
            throw new RecursoNoEncontradoException("Documento no encontrado: " + id);
        d.setId(id);
        return ResponseEntity.ok(service.save(d));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (service.findById(id) == null)
            throw new RecursoNoEncontradoException("Documento no encontrado: " + id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
