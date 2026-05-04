package com.alejandrofernandez.ecoadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.alejandrofernandez.ecoadmin.modelo.Ruta;
import com.alejandrofernandez.ecoadmin.modelo.RutaTransportista;

public interface RutaTransportistaRepository extends JpaRepository<RutaTransportista, Long> {

    List<RutaTransportista> findByRutaIdAndActivoTrue(Long rutaId);

    Optional<RutaTransportista> findByRutaIdAndTransportistaId(Long rutaId, Long transportistaId);

    boolean existsByRutaIdAndTransportistaIdAndActivoTrue(Long rutaId, Long transportistaId);

    @Query("SELECT DISTINCT rt.ruta FROM RutaTransportista rt WHERE rt.transportista.id = :transId AND rt.activo = true")
    List<Ruta> findRutasByTransportista(@Param("transId") Long transId);

    @Modifying
    @Query("DELETE FROM RutaTransportista rt WHERE rt.ruta.id = :rutaId AND rt.transportista.id = :transId")
    int eliminarAsignacion(@Param("rutaId") Long rutaId, @Param("transId") Long transId);
}
