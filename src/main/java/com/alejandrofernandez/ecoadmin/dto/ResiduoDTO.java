package com.alejandrofernandez.ecoadmin.dto;

import java.time.LocalDateTime;

import com.alejandrofernandez.ecoadmin.modelo.Residuo;

public record ResiduoDTO(
        Long id,
        String codigo,
        double cantidad,
        String unidad,
        String estado,
        String codigoLER,
        String descripcion,
        LocalDateTime fechaEntradaAlmacen,
        LocalDateTime fechaSalidaAlmacen,
        Integer diasMaximoAlmacenamiento,
        CentroSummary centro
) {
    public record CentroSummary(Long id, String codigo, String nombre) {}

    public static ResiduoDTO from(Residuo r) {
        CentroSummary cs = r.getCentro() == null ? null
                : new CentroSummary(r.getCentro().getId(),
                        r.getCentro().getCodigo(), r.getCentro().getNombre());

        return new ResiduoDTO(
                r.getId(), r.getCodigo(), r.getCantidad(), r.getUnidad(),
                r.getEstado(), r.getCodigoLER(), r.getDescripcion(),
                r.getFechaEntradaAlmacen(), r.getFechaSalidaAlmacen(),
                r.getDiasMaximoAlmacenamiento(), cs
        );
    }
}
