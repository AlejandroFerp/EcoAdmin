package com.alejandrofernandez.ecoadmin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.alejandrofernandez.ecoadmin.modelo.SecuenciaCodigo;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface SecuenciaCodigoRepository extends JpaRepository<SecuenciaCodigo, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    // FlushModeType.COMMIT prevents Hibernate from auto-flushing pending entities
    // before this query executes. Without this, a @PrePersist on Direccion that
    // calls generar() would trigger a recursive auto-flush → StackOverflowError.
    @QueryHints(@QueryHint(name = "org.hibernate.flushMode", value = "COMMIT"))
    @Query("SELECT s FROM SecuenciaCodigo s WHERE s.clave = :clave")
    Optional<SecuenciaCodigo> findByClaveConBloqueo(@Param("clave") String clave);
}
