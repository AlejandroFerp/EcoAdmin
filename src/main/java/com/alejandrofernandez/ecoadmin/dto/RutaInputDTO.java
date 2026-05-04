package com.alejandrofernandez.ecoadmin.dto;

import java.time.LocalDate;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoRuta;

public record RutaInputDTO(
        String nombre,
        LocalDate fecha,
        EstadoRuta estado,
        Long origenId,
        Long destinoId,
        Double distanciaKm,
        String observaciones,
        String formulaTarifa,
        String unidadTarifa) {
}