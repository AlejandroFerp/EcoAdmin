package com.iesdoctorbalmis.spring.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;

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