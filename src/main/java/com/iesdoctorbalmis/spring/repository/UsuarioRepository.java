package com.iesdoctorbalmis.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iesdoctorbalmis.spring.modelo.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {}
