package com.alejandrofernandez.ecoadmin;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoTraslado;

class EstadoTrasladoTest {

    @ParameterizedTest(name = "{0} -> {1} debe ser valida")
    @CsvSource({
        "PENDIENTE, EN_TRANSITO",
        "PENDIENTE, ENTREGADO",
        "PENDIENTE, COMPLETADO",
        "EN_TRANSITO, PENDIENTE",
        "EN_TRANSITO, ENTREGADO",
        "EN_TRANSITO, COMPLETADO",
        "ENTREGADO, PENDIENTE",
        "ENTREGADO, EN_TRANSITO",
        "ENTREGADO, COMPLETADO",
        "COMPLETADO, PENDIENTE",
        "COMPLETADO, EN_TRANSITO",
        "COMPLETADO, ENTREGADO"
    })
    @DisplayName("Cualquier cambio de estado distinto al actual es valido")
    void transicionesValidas(EstadoTraslado origen, EstadoTraslado destino) {
        assertThat(origen.puedeTransicionarA(destino)).isTrue();
    }

    @Test
    @DisplayName("Transicion al mismo estado es invalida")
    void transicionMismoEstadoInvalida() {
        for (EstadoTraslado estado : EstadoTraslado.values()) {
            assertThat(estado.puedeTransicionarA(estado)).isFalse();
        }
    }

    @Test
    @DisplayName("Cada estado tiene al menos una salida")
    void todosLosEstadosTienenSalida() {
        for (EstadoTraslado estado : EstadoTraslado.values()) {
            boolean tieneAlMenosUnaSalida = false;
            for (EstadoTraslado destino : EstadoTraslado.values()) {
                if (estado.puedeTransicionarA(destino)) {
                    tieneAlMenosUnaSalida = true;
                    break;
                }
            }
            assertThat(tieneAlMenosUnaSalida)
                .as("Estado %s debe tener al menos una transicion valida", estado)
                .isTrue();
        }
    }
}
