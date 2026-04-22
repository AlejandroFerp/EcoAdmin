package com.iesdoctorbalmis.spring.dto;

public record PerfilEdicionDTO(
        String nombre,
        String email,
        String telefono,
        String dni,
        String cargo,
        String fotoUrl,
        Boolean notificacionesEmail
) {}
