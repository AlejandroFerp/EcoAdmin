package com.alejandrofernandez.ecoadmin.dto;

import java.time.LocalDateTime;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;

public record UsuarioDTO(
        Long id,
        String nombre,
        String email,
        Rol rol,
        LocalDateTime fechaAlta,
        String telefono,
        String dni,
        String cargo,
        String fotoUrl,
        boolean notificacionesEmail
) {
    public static UsuarioDTO from(Usuario u) {
        return new UsuarioDTO(
                u.getId(), u.getNombre(), u.getEmail(), u.getRol(), u.getFechaAlta(),
                u.getTelefono(), u.getDni(), u.getCargo(), u.getFotoUrl(), u.isNotificacionesEmail()
        );
    }
}
