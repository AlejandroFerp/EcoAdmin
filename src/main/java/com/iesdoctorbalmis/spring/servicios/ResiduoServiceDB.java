package com.iesdoctorbalmis.spring.servicios;

import java.util.List;

import org.springframework.stereotype.Service;

import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;

@Service
public class ResiduoServiceDB implements ResiduoService {

    private final ResiduoRepository repo;
    private final CentroRepository centroRepo;

    public ResiduoServiceDB(ResiduoRepository repo, CentroRepository centroRepo) {
        this.repo = repo;
        this.centroRepo = centroRepo;
    }

    @Override
    public List<Residuo> findAll() {
        return repo.findAll();
    }

    @Override
    public List<Residuo> findByUsuario(Usuario usuario) {
        List<Centro> centros = centroRepo.findByUsuario(usuario);
        if (centros.isEmpty()) return List.of();
        return repo.findByCentroIn(centros);
    }

    @Override
    public Residuo findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public Residuo save(Residuo r) {
        if (r.getCentro() != null && r.getCentro().getId() != null)
            r.setCentro(centroRepo.findById(r.getCentro().getId()).orElseThrow());
        return repo.save(r);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
