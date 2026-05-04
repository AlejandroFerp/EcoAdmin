package com.alejandrofernandez.ecoadmin.servicios;

import java.util.List;

import com.alejandrofernandez.ecoadmin.modelo.Direccion;

public interface DireccionService {

    List<Direccion> findAll();

    Direccion findById(Long id);

    Direccion save(Direccion d);

    void delete(Long id);
}
