package com.alejandrofernandez.ecoadmin.servicios;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alejandrofernandez.ecoadmin.excepciones.RecursoNoEncontradoException;
import com.alejandrofernandez.ecoadmin.modelo.Direccion;
import com.alejandrofernandez.ecoadmin.modelo.Ruta;
import com.alejandrofernandez.ecoadmin.repository.DireccionRepository;
import com.alejandrofernandez.ecoadmin.repository.RutaRepository;
import com.alejandrofernandez.ecoadmin.dto.RutaInputDTO;

@Service
public class RutaService {

    private final RutaRepository rutaRepo;
    private final DireccionRepository direccionRepo;
    private final TarifaValidator tarifaValidator;

    public RutaService(RutaRepository rutaRepo, DireccionRepository direccionRepo, TarifaValidator tarifaValidator) {
        this.rutaRepo = rutaRepo;
        this.direccionRepo = direccionRepo;
        this.tarifaValidator = tarifaValidator;
    }

    public List<Ruta> findAll() {
        return rutaRepo.findAll();
    }

    public List<Ruta> findByEstado(com.alejandrofernandez.ecoadmin.modelo.enums.EstadoRuta estado) {
        return rutaRepo.findByEstado(estado);
    }

    public Optional<Ruta> findById(Long id) {
        return rutaRepo.findById(id);
    }

    @Transactional
    public Ruta crear(RutaInputDTO datos) {
        if (datos.nombre() == null || datos.nombre().isBlank())
            throw new IllegalArgumentException("El nombre de la ruta es obligatorio.");
        validarFormula(datos.formulaTarifa());

        Ruta ruta = new Ruta();
        ruta.setNombre(datos.nombre());
        ruta.setFecha(datos.fecha());
        ruta.setEstado(datos.estado());
        ruta.setDistanciaKm(datos.distanciaKm());
        ruta.setObservaciones(datos.observaciones());
        ruta.setFormulaTarifa(datos.formulaTarifa());
        ruta.setUnidadTarifa(datos.unidadTarifa());

        if (datos.origenId() != null) {
            ruta.setOrigen(direccionRepo.findById(datos.origenId()).orElse(null));
        }
        if (datos.destinoId() != null) {
            ruta.setDestino(direccionRepo.findById(datos.destinoId()).orElse(null));
        }

        return rutaRepo.save(ruta);
    }

    @Transactional
    public Ruta actualizar(Long id, RutaInputDTO datos) {
        Ruta existente = rutaRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada: " + id));
        if (datos.nombre() == null || datos.nombre().isBlank())
            throw new IllegalArgumentException("El nombre de la ruta es obligatorio.");

        validarFormula(datos.formulaTarifa());

        existente.setNombre(datos.nombre());
        existente.setFecha(datos.fecha());
        existente.setEstado(datos.estado());
        existente.setDistanciaKm(datos.distanciaKm());
        existente.setObservaciones(datos.observaciones());
        existente.setFormulaTarifa(datos.formulaTarifa());
        existente.setUnidadTarifa(datos.unidadTarifa());

        if (datos.origenId() != null) {
            existente.setOrigen(direccionRepo.findById(datos.origenId()).orElse(null));
        } else {
            existente.setOrigen(null);
        }

        if (datos.destinoId() != null) {
            existente.setDestino(direccionRepo.findById(datos.destinoId()).orElse(null));
        } else {
            existente.setDestino(null);
        }

        return rutaRepo.save(existente);
    }

    private void validarFormula(String formula) {
        if (formula == null || formula.isBlank())
            return;
        TarifaValidator.ResultadoValidacion rv = tarifaValidator.validar(formula);
        if (!rv.valido())
            throw new IllegalArgumentException(rv.mensaje());
    }

    @Transactional
    public void eliminar(Long id) {
        if (!rutaRepo.existsById(id))
            throw new RecursoNoEncontradoException("Ruta no encontrada: " + id);
        rutaRepo.deleteById(id);
    }

}