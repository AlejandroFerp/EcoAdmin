package com.alejandrofernandez.ecoadmin;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.alejandrofernandez.ecoadmin.modelo.Residuo;
import com.alejandrofernandez.ecoadmin.servicios.ResiduoAlmacenLifecycle;

class ResiduoAlmacenLifecycleTest {

    @Test
    @DisplayName("ALMACENADO sin fecha de entrada registra entrada automatica")
    void aplicarReglasEnGuardado_registraEntrada() {
        Residuo residuo = new Residuo();
        residuo.setEstado("ALMACENADO");
        LocalDateTime ahora = LocalDateTime.of(2026, 4, 29, 10, 30);

        ResiduoAlmacenLifecycle.aplicarReglasEnGuardado(residuo, ahora);

        assertThat(residuo.getFechaEntradaAlmacen()).isEqualTo(ahora);
        assertThat(residuo.getFechaSalidaAlmacen()).isNull();
    }

    @Test
    @DisplayName("TRATADO con entrada previa registra salida automatica")
    void aplicarReglasEnGuardado_registraSalidaPorEstado() {
        Residuo residuo = new Residuo();
        residuo.setEstado("TRATADO");
        residuo.setFechaEntradaAlmacen(LocalDateTime.of(2026, 4, 1, 9, 0));
        LocalDateTime ahora = LocalDateTime.of(2026, 4, 29, 11, 0);

        ResiduoAlmacenLifecycle.aplicarReglasEnGuardado(residuo, ahora);

        assertThat(residuo.getFechaSalidaAlmacen()).isEqualTo(ahora);
    }

    @Test
    @DisplayName("Traslado completado registra salida sin sobrescribir una existente")
    void registrarSalidaPorTraslado_respetaSalidaExistente() {
        Residuo residuo = new Residuo();
        residuo.setFechaEntradaAlmacen(LocalDateTime.of(2026, 4, 1, 9, 0));
        LocalDateTime salidaOriginal = LocalDateTime.of(2026, 4, 20, 15, 0);
        residuo.setFechaSalidaAlmacen(salidaOriginal);

        ResiduoAlmacenLifecycle.registrarSalidaPorTraslado(residuo, LocalDateTime.of(2026, 4, 29, 12, 0));

        assertThat(residuo.getFechaSalidaAlmacen()).isEqualTo(salidaOriginal);
    }
}