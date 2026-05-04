package com.alejandrofernandez.ecoadmin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alejandrofernandez.ecoadmin.modelo.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
}
