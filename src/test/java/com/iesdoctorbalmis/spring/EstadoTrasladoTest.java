package com.iesdoctorbalmis.spring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;

class EstadoTrasladoTest {

    @ParameterizedTest(name = "{0} -> {1} debe ser valida")
    @CsvSource({
        "PENDIENTE, EN_TRANSITO",
        "EN_TRANSITO, ENTREGADO",
        "ENTREGADO, COMPLETADO"
    })
    @DisplayName("Transiciones validas del ciclo de vida")
    void transicionesValidas(EstadoTraslado origen, EstadoTraslado destino) {
        assertThat(origen.puedeTransicionarA(destino)).isTrue();
    }

    @ParameterizedTest(name = "{0} -> {1} debe ser invalida")
    @CsvSource({
        "PENDIENTE, ENTREGADO",
        "PENDIENTE, COMPLETADO",
        "EN_TRANSITO, PENDIENTE",
        "EN_TRANSITO, COMPLETADO",
        "ENTREGADO, PENDIENTE",
        "ENTREGADO, EN_TRANSITO",
        "COMPLETADO, PENDIENTE",
        "COMPLETADO, EN_TRANSITO",
        "COMPLETADO, ENTREGADO"
    })
    @DisplayName("Transiciones invalidas rechazadas")
    void transicionesInvalidas(EstadoTraslado origen, EstadoTraslado destino) {
        assertThat(origen.puedeTransicionarA(destino)).isFalse();
    }

    @Test
    @DisplayName("COMPLETADO no permite ninguna transicion")
    void completadoEsEstadoFinal() {
        for (EstadoTraslado destino : EstadoTraslado.values()) {
            assertThat(EstadoTraslado.COMPLETADO.puedeTransicionarA(destino)).isFalse();
        }
    }

    @Test
    @DisplayName("Cada estado tiene al menos una salida excepto COMPLETADO")
    void todosLosEstadosTienenSalida() {
        for (EstadoTraslado estado : EstadoTraslado.values()) {
            if (estado == EstadoTraslado.COMPLETADO) continue;
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
