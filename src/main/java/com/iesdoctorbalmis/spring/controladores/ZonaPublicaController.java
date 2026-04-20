package com.iesdoctorbalmis.spring.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.TrasladoRepository;

@Controller
@RequestMapping("/public")
public class ZonaPublicaController {

    @Autowired
    private CentroRepository centroRepo;

    @Autowired
    private ResiduoRepository residuoRepo;

    @Autowired
    private TrasladoRepository trasladoRepo;

    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("totalCentros",    centroRepo.count());
        model.addAttribute("totalResiduos",   residuoRepo.count());
        model.addAttribute("trasladosEnCurso",
                trasladoRepo.findByEstado(EstadoTraslado.EN_TRANSITO).size()
              + trasladoRepo.findByEstado(EstadoTraslado.PENDIENTE).size());
        model.addAttribute("trasladosCompletados",
                trasladoRepo.findByEstado(EstadoTraslado.COMPLETADO).size());
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/centros")
    public String centros() { return "centros"; }

    @GetMapping("/residuos")
    public String residuos() { return "residuos"; }

    @GetMapping("/usuarios")
    public String usuarios() { return "usuarios"; }

    @GetMapping("/traslados")
    public String traslados() { return "traslados"; }

    @GetMapping("/direcciones")
    public String direcciones() { return "direcciones"; }
}
