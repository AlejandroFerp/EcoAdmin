package com.alejandrofernandez.ecoadmin.dto;

public record CentroInputDTO(
        String nombre,
        String tipo,
        Long direccionId,
        String nima,
        String telefono,
        String email,
        String nombreContacto,
        String detalleEnvio
) {}
