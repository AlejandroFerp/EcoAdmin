package com.iesdoctorbalmis.spring.servicios;

import java.util.List;

import com.iesdoctorbalmis.spring.modelo.Centro;

public interface CentroService {

    List<Centro> findAll();

    Centro findById(Long id);

    Centro save(Centro c);

    void delete(Long id);
}

