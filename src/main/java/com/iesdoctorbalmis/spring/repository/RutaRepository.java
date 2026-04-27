package com.iesdoctorbalmis.spring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iesdoctorbalmis.spring.modelo.Ruta;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoRuta;

public interface RutaRepository extends JpaRepository<Ruta, Long> {
    List<Ruta> findByEstado(EstadoRuta estado);
}