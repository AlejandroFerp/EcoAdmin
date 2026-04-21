package com.iesdoctorbalmis.spring.controladores;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iesdoctorbalmis.spring.dto.EstadisticasDTO;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.TrasladoRepository;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Estadisticas", description = "Metricas y KPIs del sistema (traslados, residuos, centros)")
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
    public EstadisticasDTO obtener(@RequestParam(required = false) String desde) {
        LocalDateTime desdeDateTime = null;
        if (desde != null && !desde.isBlank()) {
            desdeDateTime = LocalDate.parse(desde).atStartOfDay();
        }

        Map<String, Long> residuosPorCentro = residuoRepo.findAll().stream()
                .filter(r -> r.getCentro() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getCentro().getNombre(),
                        Collectors.counting()
                ));

        if (desdeDateTime != null) {
            final LocalDateTime fd = desdeDateTime;
            return new EstadisticasDTO(
                    centroRepo.count(),
                    residuoRepo.count(),
                    trasladoRepo.countByEstadoAndFechaCreacionAfter(EstadoTraslado.PENDIENTE, fd),
                    trasladoRepo.countByEstadoAndFechaCreacionAfter(EstadoTraslado.EN_TRANSITO, fd),
                    trasladoRepo.countByEstadoAndFechaCreacionAfter(EstadoTraslado.ENTREGADO, fd),
                    trasladoRepo.countByEstadoAndFechaCreacionAfter(EstadoTraslado.COMPLETADO, fd),
                    residuosPorCentro
            );
        }

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
