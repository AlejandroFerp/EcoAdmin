package com.iesdoctorbalmis.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iesdoctorbalmis.spring.modelo.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
}
