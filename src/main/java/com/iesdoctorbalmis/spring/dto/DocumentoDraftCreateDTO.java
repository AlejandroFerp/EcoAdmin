package com.iesdoctorbalmis.spring.dto;

import java.time.LocalDate;
import java.util.Map;

import com.iesdoctorbalmis.spring.modelo.enums.TipoDocumento;

public record DocumentoDraftCreateDTO(
        TipoDocumento tipo,
        Long trasladoId,
        Long centroId,
        String numeroReferencia,
        LocalDate fechaEmision,
        LocalDate fechaVencimiento,
        String observaciones,
        Map<String, Object> metadatos
) {}
