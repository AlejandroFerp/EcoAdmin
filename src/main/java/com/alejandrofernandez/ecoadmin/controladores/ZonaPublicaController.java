package com.alejandrofernandez.ecoadmin.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoTraslado;
import com.alejandrofernandez.ecoadmin.repository.CentroRepository;
import com.alejandrofernandez.ecoadmin.repository.ResiduoRepository;
import com.alejandrofernandez.ecoadmin.repository.TrasladoRepository;


@Controller
public class ZonaPublicaController {

    private final CentroRepository centroRepo;
    private final ResiduoRepository residuoRepo;
    private final TrasladoRepository trasladoRepo;

    public ZonaPublicaController(CentroRepository centroRepo,
                                 ResiduoRepository residuoRepo,
                                 TrasladoRepository trasladoRepo) {
        this.centroRepo = centroRepo;
        this.residuoRepo = residuoRepo;
        this.trasladoRepo = trasladoRepo;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalCentros",    centroRepo.count());
        model.addAttribute("totalResiduos",   residuoRepo.count());
        model.addAttribute("trasladosEnCurso",
                trasladoRepo.countByEstado(EstadoTraslado.EN_TRANSITO)
              + trasladoRepo.countByEstado(EstadoTraslado.PENDIENTE));
        model.addAttribute("trasladosCompletados",
                trasladoRepo.countByEstado(EstadoTraslado.COMPLETADO));
        return "dashboard";
    }

    @GetMapping("/centers")
    public String centers() { return "centers"; }

    @GetMapping("/waste")
    public String waste() { return "waste"; }

    @GetMapping("/users")
    public String users() { return "users"; }

    @GetMapping("/shipments")
    public String shipments() { return "shipments"; }

    @GetMapping("/addresses")
    public String addresses() { return "addresses"; }

    @GetMapping("/documents")
    public String documents() { return "documents"; }

    @GetMapping("/routes")
    public String routes() { return "routes"; }

    @GetMapping("/business")
    public String business() { return "business"; }

    @GetMapping("/profile")
    public String profile() { return "profile"; }

    @GetMapping("/reports")
    public String reports() { return "reports"; }

    @GetMapping("/centros")
    public String legacyCenters() { return "redirect:/centers"; }

    @GetMapping("/residuos")
    public String legacyWaste() { return "redirect:/waste"; }

    @GetMapping("/usuarios")
    public String legacyUsers() { return "redirect:/users"; }

    @GetMapping("/traslados")
    public String legacyShipments() { return "redirect:/shipments"; }

    @GetMapping("/direcciones")
    public String legacyAddresses() { return "redirect:/addresses"; }

    @GetMapping("/documentos")
    public String legacyDocuments() { return "redirect:/documents"; }

    @GetMapping("/rutas")
    public String legacyRoutes() { return "redirect:/routes"; }

    @GetMapping("/negocio")
    public String legacyBusiness() { return "redirect:/business"; }

    @GetMapping("/mis-datos")
    public String legacyProfile() { return "redirect:/profile"; }

    @GetMapping("/informes")
    public String legacyReports() { return "redirect:/reports"; }

    @GetMapping("/usuarios/{id}")
    public String usuarioPerfil(@PathVariable Long id, org.springframework.ui.Model model) {
        model.addAttribute("usuarioId", id);
        return "usuario-perfil";
    }

    @GetMapping("/registro")
    public String registro() {
        return "registro";
    }

    @GetMapping("/solicitudes")
    public String solicitudes() {
        return "solicitudes";
    }
}