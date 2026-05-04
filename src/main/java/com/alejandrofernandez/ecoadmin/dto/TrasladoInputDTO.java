package com.alejandrofernandez.ecoadmin.dto;

import java.time.LocalDateTime;

public record TrasladoInputDTO(
        Long centroProductorId,
        Long centroGestorId,
        Long residuoId,
        Long transportistaId,
        String observaciones,
        LocalDateTime fechaProgramadaInicio,
        LocalDateTime fechaProgramadaFin
) {}
