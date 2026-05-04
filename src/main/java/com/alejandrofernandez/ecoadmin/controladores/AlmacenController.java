package com.alejandrofernandez.ecoadmin.controladores;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alejandrofernandez.ecoadmin.modelo.Residuo;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;
import com.alejandrofernandez.ecoadmin.servicios.ResiduoService;
import com.alejandrofernandez.ecoadmin.servicios.UsuarioAutenticadoService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Almacen", description = "Gestion FIFO del almacen de residuos")
@RestController
@RequestMapping("/api/almacen")
public class AlmacenController {

    private final ResiduoService residuoService;
    private final UsuarioAutenticadoService authService;

    public AlmacenController(ResiduoService residuoService, UsuarioAutenticadoService authService) {
        this.residuoService = residuoService;
        this.authService = authService;
    }

    /**
     * Lista de residuos actualmente en almacen, ordenados FIFO (mas antiguo primero).
     * Solo incluye residuos con fechaEntradaAlmacen != null y fechaSalidaAlmacen == null.
     */
    @GetMapping
    public List<Map<String, Object>> listarAlmacen() {
        LocalDateTime ahora = LocalDateTime.now();
        return residuosVisibles().stream()
                .filter(r -> r.getFechaEntradaAlmacen() != null && r.getFechaSalidaAlmacen() == null)
                .sorted(Comparator.comparing(Residuo::getFechaEntradaAlmacen))
                .map(r -> toItem(r, ahora))
                .toList();
    }

    /**
     * Residuos que han superado el tiempo maximo de almacenamiento (alerta FIFO).
     */
    @GetMapping("/alertas-fifo")
    public List<Map<String, Object>> alertasFifo() {
        LocalDateTime ahora = LocalDateTime.now();
        return residuosVisibles().stream()
                .filter(r -> r.getFechaEntradaAlmacen() != null && r.getFechaSalidaAlmacen() == null)
                .filter(r -> diasEnAlmacen(r, ahora) > maximo(r))
                .sorted(Comparator.comparing(Residuo::getFechaEntradaAlmacen))
                .map(r -> toItem(r, ahora))
                .toList();
    }

    private List<Residuo> residuosVisibles() {
        Usuario u = authService.obtenerUsuarioActual();
        if (u == null) return List.of();
        if (authService.esAdmin(u) || u.getRol() == Rol.GESTOR) {
            return residuoService.findAll();
        }
        return residuoService.findByUsuario(u);
    }

    private static long diasEnAlmacen(Residuo r, LocalDateTime ahora) {
        return ChronoUnit.DAYS.between(r.getFechaEntradaAlmacen(), ahora);
    }

    private static int maximo(Residuo r) {
        return r.getDiasMaximoAlmacenamiento() == null ? 180 : r.getDiasMaximoAlmacenamiento();
    }

    private static Map<String, Object> toItem(Residuo r, LocalDateTime ahora) {
        long dias = diasEnAlmacen(r, ahora);
        int max = maximo(r);
        String severidad;
        if (dias > max) severidad = "CRITICO";
        else if (dias > max * 0.8) severidad = "AVISO";
        else severidad = "OK";

        return Map.of(
                "id", r.getId(),
                "codigoLER", r.getCodigoLER() == null ? "" : r.getCodigoLER(),
                "descripcion", r.getDescripcion() == null ? "" : r.getDescripcion(),
                "cantidad", r.getCantidad(),
                "unidad", r.getUnidad() == null ? "" : r.getUnidad(),
                "centro", r.getCentro() == null ? "" : r.getCentro().getNombre(),
                "fechaEntradaAlmacen", r.getFechaEntradaAlmacen().toString(),
                "diasEnAlmacen", dias,
                "diasMaximo", max,
                "severidad", severidad
        );
    }
}
