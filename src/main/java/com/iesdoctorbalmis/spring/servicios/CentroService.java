package com.iesdoctorbalmis.spring.servicios;

import java.util.List;

import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Usuario;

public interface CentroService {

    List<Centro> findAll();

    List<Centro> findByUsuario(Usuario usuario);

    Centro findById(Long id);

    Centro save(Centro c);

    void delete(Long id);
}
