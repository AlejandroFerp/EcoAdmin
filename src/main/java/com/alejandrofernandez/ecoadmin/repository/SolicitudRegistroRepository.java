package com.alejandrofernandez.ecoadmin.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.alejandrofernandez.ecoadmin.modelo.SolicitudRegistro;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoSolicitud;

public interface SolicitudRegistroRepository extends JpaRepository<SolicitudRegistro, Long> {
    List<SolicitudRegistro> findByEstadoOrderByFechaSolicitudDesc(EstadoSolicitud estado);
    List<SolicitudRegistro> findAllByOrderByFechaSolicitudDesc();
    boolean existsByEmailAndEstado(String email, EstadoSolicitud estado);
}
