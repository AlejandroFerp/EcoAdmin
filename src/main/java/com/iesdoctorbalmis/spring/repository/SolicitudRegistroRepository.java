package com.iesdoctorbalmis.spring.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.iesdoctorbalmis.spring.modelo.SolicitudRegistro;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoSolicitud;

public interface SolicitudRegistroRepository extends JpaRepository<SolicitudRegistro, Long> {
    List<SolicitudRegistro> findByEstadoOrderByFechaSolicitudDesc(EstadoSolicitud estado);
    List<SolicitudRegistro> findAllByOrderByFechaSolicitudDesc();
    boolean existsByEmailAndEstado(String email, EstadoSolicitud estado);
}
