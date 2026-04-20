package com.iesdoctorbalmis.spring.dto;

import java.time.LocalDateTime;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;

public record UsuarioDTO(Long id, String nombre, String email, Rol rol, LocalDateTime fechaAlta) {}
