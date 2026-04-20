package com.iesdoctorbalmis.spring.controladores;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.servicios.CentroService;

@RestController
@RequestMapping("/api/centros")
@CrossOrigin("*")
public class CentroController {

    @Autowired
    private CentroService service;

    @GetMapping
    public List<Centro> listar() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Centro buscar(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public Centro crear(@RequestBody Centro c) {
        return service.save(c);
    }

    @PutMapping("/{id}")
    public Centro editar(@PathVariable Long id, @RequestBody Centro c) {
        c.setId(id);
        return service.save(c);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.delete(id);
    }
}

