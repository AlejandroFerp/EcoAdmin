package com.iesdoctorbalmis.spring.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.iesdoctorbalmis.spring.modelo.SecuenciaCodigo;

import jakarta.persistence.LockModeType;

public interface SecuenciaCodigoRepository extends JpaRepository<SecuenciaCodigo, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SecuenciaCodigo s WHERE s.clave = :clave")
    Optional<SecuenciaCodigo> findByClaveConBloqueo(@Param("clave") String clave);
}
