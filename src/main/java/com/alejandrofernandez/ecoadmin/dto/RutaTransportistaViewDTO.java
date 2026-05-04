package com.alejandrofernandez.ecoadmin.dto;

/**
 * Vista enriquecida de una asignación transportista-ruta, con la fórmula efectiva
 * (propia o fallback de la ruta) y un precio de ejemplo calculado para 100 kg.
 */
public record RutaTransportistaViewDTO(
    Long id,
    Long transportistaId,
    String nombre,
    String email,
    /** Fórmula propia del transportista para esta ruta. Null → usa la de la ruta. */
    String formulaTarifa,
    /** La fórmula que se aplicará realmente (propia o fallback). Null → sin tarifa. */
    String formulaEfectiva,
    String unidadTarifa,
    /** Precio calculado con w=100 kg y L=distanciaKm de la ruta. Null si sin fórmula. */
    Double precioEjemplo
) {}
