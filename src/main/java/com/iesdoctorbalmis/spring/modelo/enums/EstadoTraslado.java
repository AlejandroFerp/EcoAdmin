package com.iesdoctorbalmis.spring.modelo.enums;

public enum EstadoTraslado {
    PENDIENTE,
    EN_TRANSITO,
    ENTREGADO,
    COMPLETADO;

    /**
     * Permite transiciones libres entre cualquier estado para facilitar rectificaciones manuales.
     * Solo se prohibe la transicion a si mismo para evitar generar eventos vacios en el historial.
     * La trazabilidad se mantiene mediante el historial de eventos (EventoTraslado).
     */
    public boolean puedeTransicionarA(EstadoTraslado destino) {
        if (destino == null) return false;
        return this != destino;
    }
}
