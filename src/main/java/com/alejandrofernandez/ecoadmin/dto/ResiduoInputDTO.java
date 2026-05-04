package com.alejandrofernandez.ecoadmin.dto;

import java.time.LocalDateTime;

public record ResiduoInputDTO(
        Long centroId,
        double cantidad,
        String unidad,
        String estado,
        String codigoLER,
        LocalDateTime fechaEntradaAlmacen,
        Integer diasMaximoAlmacenamiento
) {}
