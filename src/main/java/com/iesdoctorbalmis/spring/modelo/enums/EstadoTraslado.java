package com.iesdoctorbalmis.spring.modelo.enums;

public enum EstadoTraslado {
    PENDIENTE,
    EN_TRANSITO,
    ENTREGADO,
    COMPLETADO;

    /**
     * Maquina de estados: solo permite transiciones lineales hacia adelante.
     * PENDIENTE -> EN_TRANSITO -> ENTREGADO -> COMPLETADO (estado final)
     */
    public boolean puedeTransicionarA(EstadoTraslado destino) {
        if (destino == null) return false;
        return switch (this) {
            case PENDIENTE    -> destino == EN_TRANSITO;
            case EN_TRANSITO  -> destino == ENTREGADO;
            case ENTREGADO    -> destino == COMPLETADO;
            case COMPLETADO   -> false;
        };
    }
}