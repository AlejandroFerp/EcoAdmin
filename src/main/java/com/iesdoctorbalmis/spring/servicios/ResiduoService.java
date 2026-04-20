package com.iesdoctorbalmis.spring.servicios;

import java.util.List;

import com.iesdoctorbalmis.spring.modelo.Residuo;

public interface ResiduoService {

    List<Residuo> findAll();

    Residuo findById(Long id);

    Residuo save(Residuo r);

    void delete(Long id);
}

