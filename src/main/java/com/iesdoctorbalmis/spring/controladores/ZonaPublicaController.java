package com.iesdoctorbalmis.spring.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.DocumentoRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.TrasladoRepository;

@Controller
public class ZonaPublicaController {

    @Autowired
    private CentroRepository centroRepo;

    @Autowired
    private ResiduoRepository residuoRepo;

    @Autowired
    private TrasladoRepository trasladoRepo;

    @Autowired
    private DocumentoRepository documentoRepo;

    @GetMapping("/public/login")
    public String login() {
        return "login";
    }

    @GetMapping("/public/index")
    public String index(Model model) {
        model.addAttribute("totalCentros",    centroRepo.count());
        model.addAttribute("totalResiduos",   residuoRepo.count());
        model.addAttribute("trasladosEnCurso",
                trasladoRepo.countByEstado(EstadoTraslado.EN_TRANSITO)
              + trasladoRepo.countByEstado(EstadoTraslado.PENDIENTE));
        model.addAttribute("trasladosCompletados",
                trasladoRepo.countByEstado(EstadoTraslado.COMPLETADO));
        return "index";
    }

    @GetMapping("/public/centros")
    public String centros() { return "centros"; }

    @GetMapping("/public/residuos")
    public String residuos() { return "residuos"; }

    @GetMapping("/public/usuarios")
    public String usuarios() { return "usuarios"; }

    @GetMapping("/public/traslados")
    public String traslados() { return "traslados"; }

    @GetMapping("/public/direcciones")
    public String direcciones() { return "direcciones"; }

    @GetMapping("/public/documentos")
    public String documentos() { return "documentos"; }

    @GetMapping("/public/rutas")
    public String rutas() { return "rutas"; }

    @GetMapping("/public/negocio")
    public String negocio() { return "negocio"; }

    @GetMapping("/public/mis-datos")
    public String misDatos() { return "mis-datos"; }
}
