package com.alejandrofernandez.ecoadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.Residuo;

public interface ResiduoRepository extends JpaRepository<Residuo, Long>, JpaSpecificationExecutor<Residuo> {

    List<Residuo> findByCentro(Centro centro);

    List<Residuo> findByCentroIn(List<Centro> centros);

    boolean existsByCentro(Centro centro);

    Optional<Residuo> findByCodigo(String codigo);
}