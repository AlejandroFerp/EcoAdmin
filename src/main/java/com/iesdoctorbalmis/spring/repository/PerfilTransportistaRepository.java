package com.iesdoctorbalmis.spring.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iesdoctorbalmis.spring.modelo.PerfilTransportista;
import com.iesdoctorbalmis.spring.modelo.Usuario;

public interface PerfilTransportistaRepository extends JpaRepository<PerfilTransportista, Long> {
    Optional<PerfilTransportista> findByUsuario(Usuario usuario);
    boolean existsByUsuario(Usuario usuario);
}
