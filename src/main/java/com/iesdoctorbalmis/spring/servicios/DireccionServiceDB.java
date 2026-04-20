package com.iesdoctorbalmis.spring.servicios;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iesdoctorbalmis.spring.modelo.Direccion;
import com.iesdoctorbalmis.spring.repository.DireccionRepository;

@Service
public class DireccionServiceDB implements DireccionService {

    @Autowired
    private DireccionRepository repo;

    @Override
    public List<Direccion> findAll() {
        return repo.findAll();
    }

    @Override
    public Direccion findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public Direccion save(Direccion d) {
        return repo.save(d);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
