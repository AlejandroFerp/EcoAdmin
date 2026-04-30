package com.iesdoctorbalmis.spring.dto;

import com.iesdoctorbalmis.spring.modelo.enums.EstadoDocumento;
import com.iesdoctorbalmis.spring.modelo.enums.TipoDocumento;

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
