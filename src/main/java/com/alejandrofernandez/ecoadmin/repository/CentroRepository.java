package com.alejandrofernandez.ecoadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.Direccion;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;

public interface CentroRepository extends JpaRepository<Centro, Long>, JpaSpecificationExecutor<Centro> {

    List<Centro> findByUsuario(Usuario usuario);

    boolean existsByDireccion(Direccion direccion);

    List<Centro> findByDireccion(Direccion direccion);

    Optional<Centro> findByCodigo(String codigo);
}