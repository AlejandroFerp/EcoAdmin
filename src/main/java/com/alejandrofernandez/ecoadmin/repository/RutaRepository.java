package com.alejandrofernandez.ecoadmin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alejandrofernandez.ecoadmin.modelo.Ruta;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoRuta;

public interface RutaRepository extends JpaRepository<Ruta, Long> {
    List<Ruta> findByEstado(EstadoRuta estado);
}