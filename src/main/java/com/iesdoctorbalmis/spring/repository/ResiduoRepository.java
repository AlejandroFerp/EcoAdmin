package com.iesdoctorbalmis.spring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Residuo;

public interface ResiduoRepository extends JpaRepository<Residuo, Long> {

    List<Residuo> findByCentro(Centro centro);

    List<Residuo> findByCentroIn(List<Centro> centros);

    boolean existsByCentro(Centro centro);

    Optional<Residuo> findByCodigo(String codigo);
}