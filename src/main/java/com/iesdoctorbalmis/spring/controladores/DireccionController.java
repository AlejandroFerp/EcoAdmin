package com.iesdoctorbalmis.spring.controladores;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iesdoctorbalmis.spring.modelo.Direccion;
import com.iesdoctorbalmis.spring.servicios.DireccionService;

@RestController
@RequestMapping("/api/direcciones")
public class DireccionController {

    @Autowired
    private DireccionService service;

    @GetMapping
    public List<Direccion> listar() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Direccion buscar(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public Direccion crear(@RequestBody Direccion d) {
        return service.save(d);
    }

    @PutMapping("/{id}")
    public Direccion editar(@PathVariable Long id, @RequestBody Direccion d) {
        d.setId(id);
        return service.save(d);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.delete(id);
    }
}
