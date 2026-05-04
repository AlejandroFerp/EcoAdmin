package com.alejandrofernandez.ecoadmin.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.Recogida;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoRecogida;

public interface RecogidaRepository extends JpaRepository<Recogida, Long>, JpaSpecificationExecutor<Recogida> {

    List<Recogida> findByEstado(EstadoRecogida estado);

    List<Recogida> findByFechaProgramadaBetween(LocalDate desde, LocalDate hasta);

    List<Recogida> findByCentroOrigen(Centro centro);

    List<Recogida> findByCentroOrigenIn(List<Centro> centros);

    List<Recogida> findByTransportista(Usuario transportista);
}
