package com.alejandrofernandez.ecoadmin.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Redirige /favicon.ico al SVG de la app para evitar que el navegador
 * muestre el icono por defecto de Tomcat en la pestaña.
 */
@Controller
public class FaviconController {

    @GetMapping("/favicon.ico")
    public String favicon() {
        return "redirect:/favicon.svg";
    }
}
