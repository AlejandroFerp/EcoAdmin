package com.iesdoctorbalmis.spring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iesdoctorbalmis.spring.modelo.EventoTraslado;
import com.iesdoctorbalmis.spring.modelo.Traslado;

public interface EventoTrasladoRepository extends JpaRepository<EventoTraslado, Long> {

    List<EventoTraslado> findByTrasladoOrderByFechaDesc(Traslado traslado);
}
