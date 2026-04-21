package com.iesdoctorbalmis.spring.controladores;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iesdoctorbalmis.spring.modelo.ListaLer;
import com.iesdoctorbalmis.spring.repository.ListaLerRepository;

@RestController
@RequestMapping("/api/lista-ler")
public class ListaLerController {

    private final ListaLerRepository repo;

    public ListaLerController(ListaLerRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<ListaLer> listar(@RequestParam(required = false) String q) {
        if (q == null || q.isBlank()) {
            return repo.findAll();
        }
        String filtro = q.toLowerCase();
        return repo.findAll().stream()
            .filter(l -> l.getCodigo().toLowerCase().contains(filtro)
                      || l.getDescripcion().toLowerCase().contains(filtro))
            .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListaLer> buscar(@PathVariable Long id) {
        return repo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
