package com.iesdoctorbalmis.spring.controladores;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iesdoctorbalmis.spring.dto.EstadisticasDTO;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.TrasladoRepository;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticasController {

    private final CentroRepository centroRepo;
    private final ResiduoRepository residuoRepo;
    private final TrasladoRepository trasladoRepo;

    public EstadisticasController(CentroRepository centroRepo, ResiduoRepository residuoRepo,
                                   TrasladoRepository trasladoRepo) {
        this.centroRepo = centroRepo;
        this.residuoRepo = residuoRepo;
        this.trasladoRepo = trasladoRepo;
    }

    @GetMapping
    public EstadisticasDTO obtener() {
        Map<String, Long> residuosPorCentro = residuoRepo.findAll().stream()
                .filter(r -> r.getCentro() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getCentro().getNombre(),
                        Collectors.counting()
                ));

        return new EstadisticasDTO(
                centroRepo.count(),
                residuoRepo.count(),
                trasladoRepo.countByEstado(EstadoTraslado.PENDIENTE),
                trasladoRepo.countByEstado(EstadoTraslado.EN_TRANSITO),
                trasladoRepo.countByEstado(EstadoTraslado.ENTREGADO),
                trasladoRepo.countByEstado(EstadoTraslado.COMPLETADO),
                residuosPorCentro
        );
    }
}
