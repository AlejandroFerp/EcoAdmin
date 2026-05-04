package com.alejandrofernandez.ecoadmin.servicios;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.repository.UsuarioRepository;

@Component
public class UsuarioAutenticadoService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAutenticadoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return usuarioRepository.findByEmail(auth.getName()).orElse(null);
    }

    public boolean esAdmin(Usuario usuario) {
        return usuario != null && usuario.getRol() == com.alejandrofernandez.ecoadmin.modelo.enums.Rol.ADMIN;
    }
}
