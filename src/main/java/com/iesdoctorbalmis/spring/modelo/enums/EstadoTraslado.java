package com.iesdoctorbalmis.spring.modelo.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum EstadoTraslado {
    PENDIENTE,
    EN_TRANSITO,
    ENTREGADO,
    COMPLETADO;

    private static final Map<EstadoTraslado, Set<EstadoTraslado>> TRANSICIONES_VALIDAS = Map.of(
        PENDIENTE,    EnumSet.of(EN_TRANSITO),
        EN_TRANSITO,  EnumSet.of(ENTREGADO),
        ENTREGADO,    EnumSet.of(COMPLETADO),
        COMPLETADO,   EnumSet.noneOf(EstadoTraslado.class)
    );

    public boolean puedeTransicionarA(EstadoTraslado destino) {
        Set<EstadoTraslado> permitidos = TRANSICIONES_VALIDAS.get(this);
        return permitidos != null && permitidos.contains(destino);
    }
}
