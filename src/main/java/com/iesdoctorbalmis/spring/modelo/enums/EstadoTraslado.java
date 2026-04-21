package com.iesdoctorbalmis.spring.modelo.enums;

public enum EstadoTraslado {
    PENDIENTE,
    EN_TRANSITO,
    ENTREGADO,
    COMPLETADO;

    /**
     * Permite transiciones libres entre cualquier estado para facilitar rectificaciones manuales.
     * La trazabilidad se mantiene mediante el historial de eventos (EventoTraslado).
     */
    public boolean puedeTransicionarA(EstadoTraslado destino) {
        if (destino == null) return false;
        // El usuario requiere libertad total para rectificar errores; el historial registra el log
        return this != destino;
    }
}