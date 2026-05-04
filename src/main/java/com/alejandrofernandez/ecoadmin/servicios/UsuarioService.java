package com.alejandrofernandez.ecoadmin.servicios;

import java.util.List;

import com.alejandrofernandez.ecoadmin.modelo.Usuario;

public interface UsuarioService {
    List<Usuario> findAll();
    Usuario findById(Long id);
    Usuario save(Usuario u);
    void delete(Long id);
}
