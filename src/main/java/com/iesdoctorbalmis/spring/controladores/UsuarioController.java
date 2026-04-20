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

import com.iesdoctorbalmis.spring.dto.UsuarioDTO;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.servicios.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    // GET todos los usuarios
    @GetMapping
    public List<UsuarioDTO> listar() {
        return service.findAll()
                    .stream()
                    .map(u -> new UsuarioDTO(u.getId(), u.getNombre(), u.getEmail()))
                    .toList();
    }


    // GET usuario por ID
    @GetMapping("/{id}")
    public UsuarioDTO buscar(@PathVariable Long id) {
        Usuario u = service.findById(id);
        if (u == null) return null; // o lanzar excepción 404
        return new UsuarioDTO(u.getId(), u.getNombre(), u.getEmail());
    }

    // POST
    @PostMapping
    public Usuario crear(@RequestBody Usuario u) {
        return service.save(u);
    }

    // PUT
    @PutMapping("/{id}")
    public Usuario editar(@PathVariable Long id, @RequestBody Usuario u) {
        u.setId(id);
        return service.save(u);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.delete(id);
    }
}
