package com.alejandrofernandez.ecoadmin.servicios;

import java.util.List;

import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;

public interface CentroService {

    List<Centro> findAll();

    List<Centro> findAllForUsuario(Usuario usuario);

    List<Centro> findByUsuario(Usuario usuario);

    Centro findById(Long id);

    Centro save(Centro c);

    void delete(Long id);
}
