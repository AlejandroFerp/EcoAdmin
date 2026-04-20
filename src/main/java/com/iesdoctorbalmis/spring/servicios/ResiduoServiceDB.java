package com.iesdoctorbalmis.spring.servicios;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;

@Service
public class ResiduoServiceDB implements ResiduoService {

    @Autowired
    private ResiduoRepository repo;

    @Override
    public List<Residuo> findAll() {
        return repo.findAll();
    }

    @Override
    public Residuo findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public Residuo save(Residuo r) {
        return repo.save(r);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
    
}

