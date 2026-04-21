package com.iesdoctorbalmis.spring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iesdoctorbalmis.spring.modelo.Documento;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoDocumento;
import com.iesdoctorbalmis.spring.modelo.enums.TipoDocumento;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findByTraslado(Traslado traslado);

    List<Documento> findByTipo(TipoDocumento tipo);

    List<Documento> findByEstado(EstadoDocumento estado);

    List<Documento> findByTrasladoOrderByCreadoEnDesc(Traslado traslado);

    boolean existsByTrasladoAndTipo(Traslado traslado, TipoDocumento tipo);
}
