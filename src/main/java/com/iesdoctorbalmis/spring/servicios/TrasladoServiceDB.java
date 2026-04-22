package com.iesdoctorbalmis.spring.servicios;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iesdoctorbalmis.spring.excepciones.RecursoNoEncontradoException;
import com.iesdoctorbalmis.spring.excepciones.TransicionEstadoInvalidaException;
import com.iesdoctorbalmis.spring.modelo.Documento;
import com.iesdoctorbalmis.spring.modelo.EventoTraslado;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoDocumento;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;
import com.iesdoctorbalmis.spring.modelo.enums.TipoDocumento;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.DocumentoRepository;
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
    private final DocumentoRepository documentoRepo;

    /**
     * Lock de proceso para serializar la generacion de referencias DI.
     * Evita la race entre dos cambios a COMPLETADO concurrentes que generarian
     * la misma referencia. Asume single-instance; en multi-nodo usar secuencia BD.
     */
    private static final Object DI_REFERENCIA_LOCK = new Object();

    public TrasladoServiceDB(TrasladoRepository trasladoRepo,
                             EventoTrasladoRepository eventoRepo,
                             CentroRepository centroRepo,
                             ResiduoRepository residuoRepo,
                             UsuarioRepository usuarioRepo,
                             DocumentoRepository documentoRepo) {
        this.trasladoRepo = trasladoRepo;
        this.eventoRepo = eventoRepo;
        this.centroRepo = centroRepo;
        this.residuoRepo = residuoRepo;
        this.usuarioRepo = usuarioRepo;
        this.documentoRepo = documentoRepo;
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
    @Transactional
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
                + ". No se permite cambiar al mismo estado.");
        }

        EventoTraslado evento = new EventoTraslado(traslado, estadoAnterior, nuevoEstado, comentario, usuario);
        eventoRepo.save(evento);

        traslado.setEstado(nuevoEstado);
        traslado.setFechaUltimoCambioEstado(LocalDateTime.now());

        if (nuevoEstado == EstadoTraslado.EN_TRANSITO && traslado.getFechaInicioTransporte() == null) {
            traslado.setFechaInicioTransporte(LocalDateTime.now());
        }
        if (nuevoEstado == EstadoTraslado.COMPLETADO || nuevoEstado == EstadoTraslado.ENTREGADO) {
            traslado.setFechaEntrega(LocalDateTime.now());
        }

        Traslado guardado = trasladoRepo.save(traslado);

        // Al completar: marcar salida del residuo y generar DI + Archivo Cronologico automaticos
        if (nuevoEstado == EstadoTraslado.COMPLETADO) {
            if (traslado.getResiduo() != null) {
                traslado.getResiduo().setFechaSalidaAlmacen(LocalDateTime.now());
                residuoRepo.save(traslado.getResiduo());
            }
            generarDocumentoIdentificacionSiNoExiste(guardado);
            generarArchivoCronologicoSiNoExiste(guardado);
        }

        return guardado;
    }

    /**
     * Genera el Documento de Identificacion (DI) asociado al traslado si aun no existe.
     * La referencia sigue el patron DI-{anio}-{NNN} con secuencia reiniciada por anio.
     * Se serializa con un lock de proceso porque el contador depende del estado actual de la BD;
     * dos transacciones simultaneas podrian generar la misma referencia.
     */
    private void generarDocumentoIdentificacionSiNoExiste(Traslado traslado) {
        if (documentoRepo.existsByTrasladoAndTipo(traslado, TipoDocumento.DOCUMENTO_IDENTIFICACION)) {
            return;
        }
        synchronized (DI_REFERENCIA_LOCK) {
            // Re-check dentro del lock por si otra hebra creo el DI mientras esperabamos
            if (documentoRepo.existsByTrasladoAndTipo(traslado, TipoDocumento.DOCUMENTO_IDENTIFICACION)) {
                return;
            }
            String anio = String.valueOf(java.time.LocalDate.now().getYear());
            String prefijo = "DI-" + anio + "-";
            long siguiente = documentoRepo.countByTipoAndNumeroReferenciaStartingWith(
                    TipoDocumento.DOCUMENTO_IDENTIFICACION, prefijo) + 1;
            String referencia = prefijo + String.format("%03d", siguiente);

            Documento di = new Documento(TipoDocumento.DOCUMENTO_IDENTIFICACION, traslado, referencia);
            di.setEstado(EstadoDocumento.EMITIDO);
            di.setObservaciones("DI generado al completar traslado #" + traslado.getId());
            documentoRepo.save(di);
        }
    }

    /**
     * Genera el documento de Archivo Cronologico (RD 553/2020) asociado al traslado si aun no existe.
     * Patron de referencia AC-{anio}-{NNN} con secuencia reiniciada por anio.
     * Se serializa con el mismo lock que el DI para evitar races en el contador.
     */
    private void generarArchivoCronologicoSiNoExiste(Traslado traslado) {
        if (documentoRepo.existsByTrasladoAndTipo(traslado, TipoDocumento.ARCHIVO_CRONOLOGICO)) {
            return;
        }
        synchronized (DI_REFERENCIA_LOCK) {
            if (documentoRepo.existsByTrasladoAndTipo(traslado, TipoDocumento.ARCHIVO_CRONOLOGICO)) {
                return;
            }
            String anio = String.valueOf(java.time.LocalDate.now().getYear());
            String prefijo = "AC-" + anio + "-";
            long siguiente = documentoRepo.countByTipoAndNumeroReferenciaStartingWith(
                    TipoDocumento.ARCHIVO_CRONOLOGICO, prefijo) + 1;
            String referencia = prefijo + String.format("%03d", siguiente);

            Documento ac = new Documento(TipoDocumento.ARCHIVO_CRONOLOGICO, traslado, referencia);
            ac.setEstado(EstadoDocumento.EMITIDO);
            ac.setObservaciones("Archivo cronologico generado al completar traslado #" + traslado.getId());
            documentoRepo.save(ac);
        }
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
