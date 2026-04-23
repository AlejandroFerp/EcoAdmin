package com.iesdoctorbalmis.spring.dto;

import java.time.LocalDate;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoRuta;

public record RutaInputDTO(
    String nombre,
    Long transportistaId,
    LocalDate fecha,
    EstadoRuta estado,
    String origenDireccion,
    String destinoDireccion,
    Double distanciaKm,
    String observaciones,
    String formulaTarifa,
    String unidadTarifa
) {}