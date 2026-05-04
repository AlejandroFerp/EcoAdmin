package com.alejandrofernandez.ecoadmin.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.Recogida;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoRecogida;

public interface RecogidaRepository extends JpaRepository<Recogida, Long> {

    List<Recogida> findByEstado(EstadoRecogida estado);

    List<Recogida> findByFechaProgramadaBetween(LocalDate desde, LocalDate hasta);

    List<Recogida> findByCentroOrigen(Centro centro);

    List<Recogida> findByCentroOrigenIn(List<Centro> centros);
}
