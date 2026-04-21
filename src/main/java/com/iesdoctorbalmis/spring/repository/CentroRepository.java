package com.iesdoctorbalmis.spring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Usuario;

public interface CentroRepository extends JpaRepository<Centro, Long> {

    List<Centro> findByUsuario(Usuario usuario);
}
