package com.iesdoctorbalmis.spring.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Recogida;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoRecogida;

public interface RecogidaRepository extends JpaRepository<Recogida, Long> {

    List<Recogida> findByEstado(EstadoRecogida estado);

    List<Recogida> findByFechaProgramadaBetween(LocalDate desde, LocalDate hasta);

    List<Recogida> findByCentroOrigen(Centro centro);

    List<Recogida> findByCentroOrigenIn(List<Centro> centros);
}
