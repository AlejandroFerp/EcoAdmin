package com.iesdoctorbalmis.spring.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iesdoctorbalmis.spring.modelo.ListaLer;

public interface ListaLerRepository extends JpaRepository<ListaLer, Long> {

    Optional<ListaLer> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
