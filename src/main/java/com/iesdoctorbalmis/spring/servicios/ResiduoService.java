package com.iesdoctorbalmis.spring.servicios;

import java.util.List;

import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.modelo.Usuario;

public interface ResiduoService {

    List<Residuo> findAll();

    List<Residuo> findByUsuario(Usuario usuario);

    Residuo findById(Long id);

    Residuo save(Residuo r);

    void delete(Long id);
}
