package com.alejandrofernandez.ecoadmin.servicios;

import java.util.List;

import com.alejandrofernandez.ecoadmin.modelo.Documento;
import com.alejandrofernandez.ecoadmin.modelo.Traslado;
import com.alejandrofernandez.ecoadmin.modelo.enums.TipoDocumento;

public interface DocumentoService {

    List<Documento> findAll();

    Documento findById(Long id);

    Documento save(Documento d);

    void delete(Long id);

    List<Documento> findByTraslado(Traslado traslado);

    Documento generarDiParaTraslado(Traslado traslado);

    boolean existeDiParaTraslado(Traslado traslado);
}
