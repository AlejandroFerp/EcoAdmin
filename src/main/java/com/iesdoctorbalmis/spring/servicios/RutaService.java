package com.iesdoctorbalmis.spring.servicios;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iesdoctorbalmis.spring.excepciones.RecursoNoEncontradoException;
import com.iesdoctorbalmis.spring.modelo.Ruta;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;
import com.iesdoctorbalmis.spring.repository.RutaRepository;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;

@Service
public class RutaService {

    private final RutaRepository rutaRepo;
    private final UsuarioRepository usuarioRepo;

    public RutaService(RutaRepository rutaRepo, UsuarioRepository usuarioRepo) {
        this.rutaRepo = rutaRepo;
        this.usuarioRepo = usuarioRepo;
    }

    public List<Ruta> findAll() { return rutaRepo.findAll(); }

    public List<Ruta> findByTransportistaId(Long id) { return rutaRepo.findByTransportistaId(id); }

    public Optional<Ruta> findById(Long id) { return rutaRepo.findById(id); }

    @Transactional
    public Ruta crear(Ruta datos, Long transportistaId) {
        if (datos.getNombre() == null || datos.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre de la ruta es obligatorio.");
        datos.setTransportista(resolverTransportista(transportistaId));
        return rutaRepo.save(datos);
    }

    @Transactional
    public Ruta actualizar(Long id, Ruta datos, Long transportistaId) {
        Ruta existente = rutaRepo.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada: " + id));
        if (datos.getNombre() == null || datos.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre de la ruta es obligatorio.");
        existente.setNombre(datos.getNombre());
        existente.setFecha(datos.getFecha());
        existente.setEstado(datos.getEstado());
        existente.setOrigenDireccion(datos.getOrigenDireccion());
        existente.setDestinoDireccion(datos.getDestinoDireccion());
        existente.setDistanciaKm(datos.getDistanciaKm());
        existente.setObservaciones(datos.getObservaciones());
        existente.setTransportista(resolverTransportista(transportistaId));
        return rutaRepo.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!rutaRepo.existsById(id))
            throw new RecursoNoEncontradoException("Ruta no encontrada: " + id);
        rutaRepo.deleteById(id);
    }

    private Usuario resolverTransportista(Long id) {
        if (id == null) return null;
        Usuario u = usuarioRepo.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Transportista no encontrado: " + id));
        if (u.getRol() != Rol.TRANSPORTISTA)
            throw new IllegalArgumentException("El usuario asignado debe tener rol TRANSPORTISTA.");
        return u;
    }
}