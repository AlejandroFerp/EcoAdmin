package com.alejandrofernandez.ecoadmin.dto;

import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoDocumento;
import com.alejandrofernandez.ecoadmin.modelo.enums.TipoDocumento;

public record DocumentoWorkflowDTO(
        Long id,
        String codigo,
        TipoDocumento tipo,
        EstadoDocumento estado,
        String numeroReferencia,
        boolean requiereAdjunto,
        boolean tieneArchivo,
        String archivoUrl,
        String siguienteAccion
) {}
