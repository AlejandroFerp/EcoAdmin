package com.alejandrofernandez.ecoadmin.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.alejandrofernandez.ecoadmin.modelo.Notificacion;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByDestinatarioOrderByFechaDesc(Usuario destinatario);
    List<Notificacion> findByDestinatarioAndLeidaFalseOrderByFechaDesc(Usuario destinatario);
    long countByDestinatarioAndLeidaFalse(Usuario destinatario);
}
