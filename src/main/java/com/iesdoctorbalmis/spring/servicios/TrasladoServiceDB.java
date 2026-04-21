package com.iesdoctorbalmis.spring.servicios;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iesdoctorbalmis.spring.excepciones.RecursoNoEncontradoException;
import com.iesdoctorbalmis.spring.excepciones.TransicionEstadoInvalidaException;
import com.iesdoctorbalmis.spring.modelo.EventoTraslado;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.EventoTrasladoRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.TrasladoRepository;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;

@Service
public class TrasladoServiceDB implements TrasladoService {

    private final TrasladoRepository trasladoRepo;
    private final EventoTrasladoRepository eventoRepo;
    private final CentroRepository centroRepo;
    private final ResiduoRepository residuoRepo;
    private final UsuarioRepository usuarioRepo;

    public TrasladoServiceDB(TrasladoRepository trasladoRepo,
                             EventoTrasladoRepository eventoRepo,
                             CentroRepository centroRepo,
                             ResiduoRepository residuoRepo,
                             UsuarioRepository usuarioRepo) {
        this.trasladoRepo = trasladoRepo;
        this.eventoRepo = eventoRepo;
        this.centroRepo = centroRepo;
        this.residuoRepo = residuoRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public List<Traslado> findAll() {
        return trasladoRepo.findAll();
    }

    @Override
    public Traslado findById(Long id) {
        return trasladoRepo.findById(id).orElse(null);
    }

    @Override
    public Traslado save(Traslado t) {
        if (t.getCentroProductor() != null && t.getCentroProductor().getId() != null)
            t.setCentroProductor(centroRepo.findById(t.getCentroProductor().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Centro productor no encontrado")));
        if (t.getCentroGestor() != null && t.getCentroGestor().getId() != null)
            t.setCentroGestor(centroRepo.findById(t.getCentroGestor().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Centro gestor no encontrado")));
        if (t.getResiduo() != null && t.getResiduo().getId() != null)
            t.setResiduo(residuoRepo.findById(t.getResiduo().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Residuo no encontrado")));
        if (t.getTransportista() != null && t.getTransportista().getId() != null)
            t.setTransportista(usuarioRepo.findById(t.getTransportista().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Transportista no encontrado")));
        return trasladoRepo.save(t);
    }

    @Override
    public void delete(Long id) {
        trasladoRepo.deleteById(id);
    }

    @Override
    public List<Traslado> findByEstado(EstadoTraslado estado) {
        return trasladoRepo.findByEstado(estado);
    }

    @Override
    @Transactional
    public Traslado cambiarEstado(Long id, EstadoTraslado nuevoEstado, String comentario, Usuario usuario) {
        Traslado traslado = trasladoRepo.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Traslado no encontrado: " + id));

        EstadoTraslado estadoAnterior = traslado.getEstado();

        if (!estadoAnterior.puedeTransicionarA(nuevoEstado)) {
            throw new TransicionEstadoInvalidaException(
                "Transicion invalida: " + estadoAnterior + " -> " + nuevoEstado
                + ". Transiciones permitidas desde " + estadoAnterior + " son limitadas.");
        }

        EventoTraslado evento = new EventoTraslado(traslado, estadoAnterior, nuevoEstado, comentario, usuario);
        eventoRepo.save(evento);

        traslado.setEstado(nuevoEstado);

        if (nuevoEstado == EstadoTraslado.EN_TRANSITO && traslado.getFechaInicioTransporte() == null) {
            traslado.setFechaInicioTransporte(LocalDateTime.now());
        }
        if (nuevoEstado == EstadoTraslado.COMPLETADO || nuevoEstado == EstadoTraslado.ENTREGADO) {
            traslado.setFechaEntrega(LocalDateTime.now());
        }

        return trasladoRepo.save(traslado);
    }

    @Override
    public List<EventoTraslado> historialDeTraslado(Long id) {
        Traslado traslado = trasladoRepo.findById(id).orElse(null);
        if (traslado == null) return List.of();
        return eventoRepo.findByTrasladoOrderByFechaDescIdDesc(traslado);
    }

    @Override
    public List<Traslado> findByUsuario(Usuario usuario) {
        return trasladoRepo.findByCentroProductorUsuario(usuario);
    }

    @Override
    public List<Traslado> findByGestor(Usuario usuario) {
        return trasladoRepo.findByCentroGestorUsuario(usuario);
    }

    @Override
    public List<Traslado> findByTransportista(Usuario usuario) {
        return trasladoRepo.findByTransportista(usuario);
    }
}
