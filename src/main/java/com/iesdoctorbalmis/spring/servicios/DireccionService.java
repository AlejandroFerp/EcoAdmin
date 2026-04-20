package com.iesdoctorbalmis.spring.servicios;

import java.util.List;

import com.iesdoctorbalmis.spring.modelo.Direccion;

public interface DireccionService {

    List<Direccion> findAll();

    Direccion findById(Long id);

    Direccion save(Direccion d);

    void delete(Long id);
}
