package com.alejandrofernandez.ecoadmin.servicios;

import java.util.List;

import com.alejandrofernandez.ecoadmin.modelo.EventoTraslado;
import com.alejandrofernandez.ecoadmin.modelo.Traslado;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoTraslado;

public interface TrasladoService {

    List<Traslado> findAll();

    List<Traslado> findAllForUsuario(Usuario usuario);

    Traslado findById(Long id);

    Traslado save(Traslado t);

    void delete(Long id);

    List<Traslado> findByEstado(EstadoTraslado estado);

    Traslado cambiarEstado(Long id, EstadoTraslado nuevoEstado, String comentario, Usuario usuario);

    List<EventoTraslado> historialDeTraslado(Long id);

    List<Traslado> findByUsuario(Usuario usuario);

    List<Traslado> findByGestor(Usuario usuario);

    List<Traslado> findByTransportista(Usuario usuario);

    Traslado asignarRuta(Long trasladoId, Long rutaId);
}
