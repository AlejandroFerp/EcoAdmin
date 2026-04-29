package com.iesdoctorbalmis.spring.servicios;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.iesdoctorbalmis.spring.excepciones.RecursoNoEncontradoException;
import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Recogida;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoRecogida;
import com.iesdoctorbalmis.spring.repository.RecogidaRepository;

@Service
public class RecogidaService {

    private final RecogidaRepository repo;

    public RecogidaService(RecogidaRepository repo) {
        this.repo = repo;
    }

    public List<Recogida> findAll() {
        return repo.findAll();
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
