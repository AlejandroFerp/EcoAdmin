package com.alejandrofernandez.ecoadmin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alejandrofernandez.ecoadmin.modelo.EventoTraslado;
import com.alejandrofernandez.ecoadmin.modelo.Traslado;

public interface EventoTrasladoRepository extends JpaRepository<EventoTraslado, Long> {

    List<EventoTraslado> findByTrasladoOrderByFechaDescIdDesc(Traslado traslado);
}