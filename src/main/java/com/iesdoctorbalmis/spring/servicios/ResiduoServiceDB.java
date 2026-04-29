package com.iesdoctorbalmis.spring.servicios;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;

import jakarta.persistence.EntityManager;

@Service
public class ResiduoServiceDB implements ResiduoService {

    private final ResiduoRepository repo;
    private final CentroRepository centroRepo;
    private final EntityManager entityManager;
    private final LerCodeResolver lerCodeResolver;

    public ResiduoServiceDB(ResiduoRepository repo,
            CentroRepository centroRepo,
            EntityManager entityManager,
            LerCodeResolver lerCodeResolver) {
        this.repo = repo;
        this.centroRepo = centroRepo;
        this.entityManager = entityManager;
        this.lerCodeResolver = lerCodeResolver;
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
    @Transactional
    public Residuo save(Residuo r) {
        CodigoInmutableSupport.conservarSiAusente(r.getId(), r.getCodigo(), repo::findById, Residuo::getCodigo, r::setCodigo);
        if (r.getCentro() != null && r.getCentro().getId() != null)
            r.setCentro(centroRepo.findById(r.getCentro().getId()).orElseThrow());
        r.setCodigoLER(lerCodeResolver.requireCanonicalCode(r.getCodigoLER()));

        // Defaults para FIFO
        if (r.getDiasMaximoAlmacenamiento() == null) {
            r.setDiasMaximoAlmacenamiento(180);
        }
        ResiduoAlmacenLifecycle.aplicarReglasEnGuardado(r, LocalDateTime.now());
        Residuo saved = repo.save(r);
        entityManager.flush();
        entityManager.clear();
        return saved.getId() == null ? saved : repo.findById(saved.getId()).orElse(saved);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
