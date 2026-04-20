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

import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.servicios.ResiduoService;

@RestController
@RequestMapping("/api/residuos")
public class ResiduoController {

    @Autowired
    private ResiduoService service;

    @GetMapping
    public List<Residuo> listar() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Residuo buscar(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public Residuo crear(@RequestBody Residuo r) {
        return service.save(r);
    }

    @PutMapping("/{id}")
    public Residuo editar(@PathVariable Long id, @RequestBody Residuo r) {
        r.setId(id);
        return service.save(r);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.delete(id);
    }
}

