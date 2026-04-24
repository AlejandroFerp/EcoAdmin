package com.iesdoctorbalmis.spring.servicios;

import java.util.List;

import com.iesdoctorbalmis.spring.modelo.EventoTraslado;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;

public interface TrasladoService {

    List<Traslado> findAll();

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
