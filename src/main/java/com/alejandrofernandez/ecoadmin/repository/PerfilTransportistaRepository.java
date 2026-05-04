package com.alejandrofernandez.ecoadmin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alejandrofernandez.ecoadmin.modelo.PerfilTransportista;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;

public interface PerfilTransportistaRepository extends JpaRepository<PerfilTransportista, Long> {
    Optional<PerfilTransportista> findByUsuario(Usuario usuario);
    boolean existsByUsuario(Usuario usuario);
}
