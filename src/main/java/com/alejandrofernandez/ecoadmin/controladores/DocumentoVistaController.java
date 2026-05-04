package com.alejandrofernandez.ecoadmin.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.alejandrofernandez.ecoadmin.excepciones.RecursoNoEncontradoException;
import com.alejandrofernandez.ecoadmin.modelo.Documento;
import com.alejandrofernandez.ecoadmin.servicios.DocumentoService;

/**
 * Vista HTML imprimible/previsualizable de un documento.
 * Sirve plantillas Thymeleaf que el frontend embebe en un iframe (popup) y que
 * tambien pueden imprimirse o "Guardar como PDF" desde el navegador.
 */
@Controller
@RequestMapping({"/preview/documents", "/preview/documentos"})
public class DocumentoVistaController {

    private final DocumentoService service;

    public DocumentoVistaController(DocumentoService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ModelAndView preview(@PathVariable Long id, Model model) {
        Documento d = service.findById(id);
        if (d == null) throw new RecursoNoEncontradoException("Documento no encontrado: " + id);
        model.addAttribute("doc", d);
        model.addAttribute("traslado", d.getTraslado());
        model.addAttribute("centro", d.getCentro());
        model.addAttribute("tipo", d.getTipo() != null ? d.getTipo().name() : "DOCUMENTO");
        return new ModelAndView("documents/preview");
    }
}
