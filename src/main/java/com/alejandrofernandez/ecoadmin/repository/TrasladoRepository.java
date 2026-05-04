package com.alejandrofernandez.ecoadmin.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.Residuo;
import com.alejandrofernandez.ecoadmin.modelo.Traslado;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoTraslado;

public interface TrasladoRepository extends JpaRepository<Traslado, Long> {

    List<Traslado> findByEstado(EstadoTraslado estado);

    long countByEstado(EstadoTraslado estado);

    long countByEstadoAndFechaCreacionAfter(EstadoTraslado estado, LocalDateTime desde);

    long countByFechaCreacionAfter(LocalDateTime desde);

    List<Traslado> findByCentroProductor(Centro centro);

    List<Traslado> findByCentroGestor(Centro centro);

    List<Traslado> findByTransportista(Usuario transportista);

    List<Traslado> findByCentroProductorUsuario(Usuario usuario);

    boolean existsByCentroProductorOrCentroGestor(Centro productor, Centro gestor);

    List<Traslado> findByCentroGestorUsuario(Usuario usuario);

    boolean existsByResiduo(Residuo residuo);

    Optional<Traslado> findByCodigo(String codigo);
}