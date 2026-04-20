package com.iesdoctorbalmis.spring.servicios;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.repository.CentroRepository;

@Service
public class CentroServiceDB implements CentroService {

    @Autowired
    private CentroRepository repo;

    @Override
    public List<Centro> findAll() {
        return repo.findAll();
    }

    @Override
    public Centro findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public Centro save(Centro c) {
        return repo.save(c);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
