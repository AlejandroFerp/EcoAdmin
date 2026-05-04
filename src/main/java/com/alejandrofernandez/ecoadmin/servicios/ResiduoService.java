package com.alejandrofernandez.ecoadmin.servicios;

import java.util.List;

import com.alejandrofernandez.ecoadmin.modelo.Residuo;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;

public interface ResiduoService {

    List<Residuo> findAll();

    List<Residuo> findByUsuario(Usuario usuario);

    Residuo findById(Long id);

    Residuo save(Residuo r);

    void delete(Long id);
}
