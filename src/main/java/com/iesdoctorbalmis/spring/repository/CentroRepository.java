package com.iesdoctorbalmis.spring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Direccion;
import com.iesdoctorbalmis.spring.modelo.Usuario;

public interface CentroRepository extends JpaRepository<Centro, Long> {

    List<Centro> findByUsuario(Usuario usuario);

    boolean existsByDireccion(Direccion direccion);

    List<Centro> findByDireccion(Direccion direccion);

    Optional<Centro> findByCodigo(String codigo);
}