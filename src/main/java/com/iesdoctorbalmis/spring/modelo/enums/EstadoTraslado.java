package com.iesdoctorbalmis.spring.modelo.enums;

public enum EstadoTraslado {
    PENDIENTE,
    EN_TRANSITO,
    ENTREGADO,
    COMPLETADO;

    /**
     * Permite cualquier transicion entre estados distintos.
     * Las restricciones direccionales se pueden imponer mas tarde si el negocio lo exige.
     */
    public boolean puedeTransicionarA(EstadoTraslado destino) {
        return destino != null && destino != this;
    }
}