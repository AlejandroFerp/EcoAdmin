package com.iesdoctorbalmis.spring.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.iesdoctorbalmis.spring.modelo.Notificacion;
import com.iesdoctorbalmis.spring.modelo.Usuario;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByDestinatarioOrderByFechaDesc(Usuario destinatario);
    List<Notificacion> findByDestinatarioAndLeidaFalseOrderByFechaDesc(Usuario destinatario);
    long countByDestinatarioAndLeidaFalse(Usuario destinatario);
}
