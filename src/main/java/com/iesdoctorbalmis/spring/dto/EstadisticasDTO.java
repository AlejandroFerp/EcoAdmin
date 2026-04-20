package com.iesdoctorbalmis.spring.dto;

import java.util.Map;

public record EstadisticasDTO(
    long totalCentros,
    long totalResiduos,
    long trasladosPendientes,
    long trasladosEnTransito,
    long trasladosEntregados,
    long trasladosCompletados,
    Map<String, Long> residuosPorCentro
) {}
