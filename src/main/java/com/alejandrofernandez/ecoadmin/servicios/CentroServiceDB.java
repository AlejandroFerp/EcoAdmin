package com.alejandrofernandez.ecoadmin.servicios;

import java.util.List;

import org.springframework.stereotype.Service;

import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.repository.CentroRepository;
import com.alejandrofernandez.ecoadmin.repository.DireccionRepository;

@Service
public class CentroServiceDB implements CentroService {

    private final CentroRepository repo;
    private final DireccionRepository direccionRepo;

    public CentroServiceDB(CentroRepository repo, DireccionRepository direccionRepo) {
        this.repo = repo;
        this.direccionRepo = direccionRepo;
    }

    @Override
    public List<Centro> findAll() {
        return repo.findAll();
    }

    @Override
    public List<Centro> findByUsuario(Usuario usuario) {
        return repo.findByUsuario(usuario);
    }

    @Override
    public Centro findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public Centro save(Centro c) {
        CodigoInmutableSupport.conservarSiAusente(c.getId(), c.getCodigo(), repo::findById, Centro::getCodigo, c::setCodigo);
        if (c.getDireccion() != null && c.getDireccion().getId() != null) {
            c.setDireccion(direccionRepo.findById(c.getDireccion().getId()).orElseThrow());
        }
        return repo.save(c);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
