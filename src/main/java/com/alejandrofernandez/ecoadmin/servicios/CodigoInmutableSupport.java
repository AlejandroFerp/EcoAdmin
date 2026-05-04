package com.alejandrofernandez.ecoadmin.servicios;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

final class CodigoInmutableSupport {

    private CodigoInmutableSupport() {
    }

    /**
     * Conserva el codigo visible ya persistido cuando una edicion llega sin ese campo.
     */
    static <T> void conservarSiAusente(
            Long id,
            String codigoActual,
            Function<Long, Optional<T>> buscarExistente,
            Function<T, String> extraerCodigo,
            Consumer<String> asignarCodigo) {

        if (id == null || (codigoActual != null && !codigoActual.isBlank())) {
            return;
        }

        T existente = buscarExistente.apply(id).orElse(null);
        if (existente == null) {
            return;
        }

        String codigoExistente = extraerCodigo.apply(existente);
        if (codigoExistente != null && !codigoExistente.isBlank()) {
            asignarCodigo.accept(codigoExistente);
        }
    }
}