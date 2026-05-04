package com.alejandrofernandez.ecoadmin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.GestorCentro;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;

public interface GestorCentroRepository extends JpaRepository<GestorCentro, Long> {

    List<GestorCentro> findByGestor(Usuario gestor);

    List<GestorCentro> findByCentro(Centro centro);

    boolean existsByGestorAndCentro(Usuario gestor, Centro centro);

    void deleteByGestorAndCentro(Usuario gestor, Centro centro);
}
