package com.iesdoctorbalmis.spring.controladores;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iesdoctorbalmis.spring.dto.EstadisticasDTO;
import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.TrasladoRepository;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticasController {

    @Autowired
    private CentroRepository centroRepo;

    @Autowired
    private ResiduoRepository residuoRepo;

    @Autowired
    private TrasladoRepository trasladoRepo;

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
                trasladoRepo.findByEstado(EstadoTraslado.PENDIENTE).size(),
                trasladoRepo.findByEstado(EstadoTraslado.EN_TRANSITO).size(),
                trasladoRepo.findByEstado(EstadoTraslado.ENTREGADO).size(),
                trasladoRepo.findByEstado(EstadoTraslado.COMPLETADO).size(),
                residuosPorCentro
        );
    }
}
