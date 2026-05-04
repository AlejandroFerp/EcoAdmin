package com.alejandrofernandez.ecoadmin.servicios;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alejandrofernandez.ecoadmin.excepciones.RecursoNoEncontradoException;
import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.Recogida;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoRecogida;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;
import com.alejandrofernandez.ecoadmin.repository.RecogidaRepository;
import com.alejandrofernandez.ecoadmin.servicios.specifications.RecogidaSpecifications;

@Service
public class RecogidaService {

    private final RecogidaRepository repo;
    private final OwnershipService ownershipService;

    public RecogidaService(RecogidaRepository repo, OwnershipService ownershipService) {
        this.repo = repo;
        this.ownershipService = ownershipService;
    }

    public List<Recogida> findAll() {
        return repo.findAll();
    }

    public List<Recogida> findAllForUsuario(Usuario usuario) {
        if (usuario.getRol() == Rol.ADMIN) {
            return repo.findAll();
        }
        var centroIds = ownershipService.getCentrosPermitidos(usuario).stream()
                .map(Centro::getId).toList();
        return repo.findAll(RecogidaSpecifications.deUsuario(usuario, centroIds));
    }

    public Recogida findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Recogida no encontrada: " + id));
    }

    public Recogida save(Recogida r) {
        CodigoInmutableSupport.conservarSiAusente(r.getId(), r.getCodigo(), repo::findById, Recogida::getCodigo, r::setCodigo);
        if (r.getEstado() == null) r.setEstado(EstadoRecogida.PROGRAMADA);
        if (r.getEstado() == EstadoRecogida.COMPLETADA && r.getFechaRealizada() == null) {
            r.setFechaRealizada(LocalDate.now());
        }
        return repo.save(r);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public List<Recogida> findByEstado(EstadoRecogida estado) {
        return repo.findByEstado(estado);
    }

    public List<Recogida> findByRango(LocalDate desde, LocalDate hasta) {
        return repo.findByFechaProgramadaBetween(desde, hasta);
    }

    public List<Recogida> findByCentros(List<Centro> centros) {
        if (centros == null || centros.isEmpty()) return List.of();
        return repo.findByCentroOrigenIn(centros);
    }
}
