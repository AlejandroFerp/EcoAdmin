package com.alejandrofernandez.ecoadmin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alejandrofernandez.ecoadmin.modelo.Documento;
import com.alejandrofernandez.ecoadmin.modelo.Traslado;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoDocumento;
import com.alejandrofernandez.ecoadmin.modelo.enums.TipoDocumento;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findByTraslado(Traslado traslado);

    List<Documento> findByTipo(TipoDocumento tipo);

    List<Documento> findByEstado(EstadoDocumento estado);

    List<Documento> findByTrasladoOrderByCreadoEnDesc(Traslado traslado);

    boolean existsByTrasladoAndTipo(Traslado traslado, TipoDocumento tipo);

    /**
     * Cuenta documentos de un tipo cuya referencia empieza por el prefijo dado.
     * Se usa para numerar correlativamente las referencias por anio
     * (p.ej. prefijo "DI-2026-" para reiniciar la secuencia cada anio).
     */
    long countByTipoAndNumeroReferenciaStartingWith(TipoDocumento tipo, String prefijo);
}
