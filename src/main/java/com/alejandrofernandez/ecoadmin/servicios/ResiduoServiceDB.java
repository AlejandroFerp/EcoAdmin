package com.alejandrofernandez.ecoadmin.servicios;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.Residuo;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;
import com.alejandrofernandez.ecoadmin.repository.CentroRepository;
import com.alejandrofernandez.ecoadmin.repository.ResiduoRepository;
import com.alejandrofernandez.ecoadmin.servicios.specifications.ResiduoSpecifications;

import jakarta.persistence.EntityManager;

@Service
public class ResiduoServiceDB implements ResiduoService {

    private final ResiduoRepository repo;
    private final CentroRepository centroRepo;
    private final EntityManager entityManager;
    private final LerCodeResolver lerCodeResolver;
    private final OwnershipService ownershipService;

    public ResiduoServiceDB(ResiduoRepository repo,
            CentroRepository centroRepo,
            EntityManager entityManager,
            LerCodeResolver lerCodeResolver,
            OwnershipService ownershipService) {
        this.repo = repo;
        this.centroRepo = centroRepo;
        this.entityManager = entityManager;
        this.lerCodeResolver = lerCodeResolver;
        this.ownershipService = ownershipService;
    }

    @Override
    public List<Residuo> findAll() {
        return repo.findAll();
    }

    @Override
    public List<Residuo> findAllForUsuario(Usuario usuario) {
        if (usuario.getRol() == Rol.ADMIN) {
            return repo.findAll();
        }
        var centroIds = ownershipService.getCentrosPermitidos(usuario).stream()
                .map(Centro::getId).toList();
        return repo.findAll(ResiduoSpecifications.deUsuario(usuario, centroIds));
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
