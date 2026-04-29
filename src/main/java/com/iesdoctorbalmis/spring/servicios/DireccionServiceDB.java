package com.iesdoctorbalmis.spring.servicios;

import java.util.List;

import org.springframework.stereotype.Service;

import com.iesdoctorbalmis.spring.modelo.Direccion;
import com.iesdoctorbalmis.spring.repository.DireccionRepository;

@Service
public class DireccionServiceDB implements DireccionService {

    private final DireccionRepository repo;

    public DireccionServiceDB(DireccionRepository repo) {
        this.repo = repo;
    }

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
        CodigoInmutableSupport.conservarSiAusente(d.getId(), d.getCodigo(), repo::findById, Direccion::getCodigo, d::setCodigo);
        return repo.save(d);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
