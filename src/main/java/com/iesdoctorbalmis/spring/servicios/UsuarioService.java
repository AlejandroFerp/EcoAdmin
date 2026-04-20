package com.iesdoctorbalmis.spring.servicios;

import java.util.List;

import com.iesdoctorbalmis.spring.modelo.Usuario;

public interface UsuarioService {
    List<Usuario> findAll();
    Usuario findById(Long id);
    Usuario save(Usuario u);
    void delete(Long id);
}
