package com.iesdoctorbalmis.spring.servicios;

import java.time.LocalDateTime;

import com.iesdoctorbalmis.spring.modelo.Residuo;

public final class ResiduoAlmacenLifecycle {

    private ResiduoAlmacenLifecycle() {
    }

    public static void aplicarReglasEnGuardado(Residuo residuo, LocalDateTime ahora) {
        if (residuo == null) {
            return;
        }

        String estado = residuo.getEstado() == null ? "" : residuo.getEstado().trim().toUpperCase();
        if ("ALMACENADO".equals(estado) && residuo.getFechaEntradaAlmacen() == null) {
            residuo.setFechaEntradaAlmacen(ahora);
        }

        if (("TRATADO".equals(estado) || "ELIMINADO".equals(estado))
                && residuo.getFechaSalidaAlmacen() == null
                && residuo.getFechaEntradaAlmacen() != null) {
            residuo.setFechaSalidaAlmacen(ahora);
        }
    }

    public static void registrarSalidaPorTraslado(Residuo residuo, LocalDateTime ahora) {
        if (residuo == null) {
            return;
        }
        if (residuo.getFechaSalidaAlmacen() == null && residuo.getFechaEntradaAlmacen() != null) {
            residuo.setFechaSalidaAlmacen(ahora);
        }
    }
}