package com.iesdoctorbalmis.spring.servicios;

import java.util.List;

import com.iesdoctorbalmis.spring.modelo.Documento;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.enums.TipoDocumento;

public interface DocumentoService {

    List<Documento> findAll();

    Documento findById(Long id);

    Documento save(Documento d);

    void delete(Long id);

    List<Documento> findByTraslado(Traslado traslado);

    Documento generarDiParaTraslado(Traslado traslado);

    boolean existeDiParaTraslado(Traslado traslado);
}
