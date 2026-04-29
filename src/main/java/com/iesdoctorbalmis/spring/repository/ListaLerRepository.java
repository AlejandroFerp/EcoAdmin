package com.iesdoctorbalmis.spring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.iesdoctorbalmis.spring.modelo.ListaLer;

public interface ListaLerRepository extends JpaRepository<ListaLer, Long> {

    Optional<ListaLer> findByCodigo(String codigo);

    List<ListaLer> findAllByDescripcionIgnoreCase(String descripcion);

    @Query("select l from ListaLer l where upper(replace(l.codigo, ' ', '')) = :normalizedCode")
    Optional<ListaLer> findByCodigoNormalizado(@Param("normalizedCode") String normalizedCode);

    boolean existsByCodigo(String codigo);
}
