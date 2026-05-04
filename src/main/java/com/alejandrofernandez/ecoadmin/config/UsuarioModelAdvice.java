package com.alejandrofernandez.ecoadmin.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.servicios.UsuarioAutenticadoService;

/**
 * Expone el usuario autenticado como atributo global del modelo, disponible
 * en todas las plantillas Thymeleaf (sidebar, header, etc.) sin que cada
 * controlador tenga que cargarlo manualmente.
 */
@ControllerAdvice(basePackages = "com.alejandrofernandez.ecoadmin.controladores")
public class UsuarioModelAdvice {

    private final UsuarioAutenticadoService usuarioAutenticado;

    public UsuarioModelAdvice(UsuarioAutenticadoService usuarioAutenticado) {
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @ModelAttribute("usuarioActual")
    public Usuario usuarioActual() {
        return usuarioAutenticado.obtenerUsuarioActual();
    }
}
