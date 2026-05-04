package com.alejandrofernandez.ecoadmin.dto;

public record RutaTransportistaInputDTO(
    Long transportistaId,
    /** Fórmula propia del transportista. Null = heredar la de la ruta. */
    String formulaTarifa,
    /** Moneda propia. Null = heredar la de la ruta. */
    String unidadTarifa
) {}
