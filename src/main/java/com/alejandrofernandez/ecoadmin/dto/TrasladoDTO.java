package com.alejandrofernandez.ecoadmin.dto;

import java.time.LocalDateTime;

import com.alejandrofernandez.ecoadmin.modelo.Traslado;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoTraslado;

public record TrasladoDTO(
        Long id,
        String codigo,
        EstadoTraslado estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaInicioTransporte,
        LocalDateTime fechaEntrega,
        LocalDateTime fechaProgramadaInicio,
        LocalDateTime fechaProgramadaFin,
        LocalDateTime fechaUltimoCambioEstado,
        String observaciones,
        CentroSummary centroProductor,
        CentroSummary centroGestor,
        ResiduoSummary residuo,
        UsuarioSummary transportista,
        RutaSummary ruta
) {
    public record CentroSummary(Long id, String codigo, String nombre, String tipo) {}
    public record ResiduoSummary(Long id, String codigo, String codigoLER, String descripcion, double cantidad, String unidad) {}
    public record UsuarioSummary(Long id, String nombre, String email) {}
    public record RutaSummary(Long id, String nombre) {}

    public static TrasladoDTO from(Traslado t) {
        CentroSummary cp = t.getCentroProductor() == null ? null
                : new CentroSummary(t.getCentroProductor().getId(), t.getCentroProductor().getCodigo(),
                        t.getCentroProductor().getNombre(), t.getCentroProductor().getTipo());

        CentroSummary cg = t.getCentroGestor() == null ? null
                : new CentroSummary(t.getCentroGestor().getId(), t.getCentroGestor().getCodigo(),
                        t.getCentroGestor().getNombre(), t.getCentroGestor().getTipo());

        ResiduoSummary rs = t.getResiduo() == null ? null
                : new ResiduoSummary(t.getResiduo().getId(), t.getResiduo().getCodigo(),
                        t.getResiduo().getCodigoLER(), t.getResiduo().getDescripcion(),
                        t.getResiduo().getCantidad(), t.getResiduo().getUnidad());

        UsuarioSummary tr = t.getTransportista() == null ? null
                : new UsuarioSummary(t.getTransportista().getId(),
                        t.getTransportista().getNombre(), t.getTransportista().getEmail());

        RutaSummary rt = t.getRuta() == null ? null
                : new RutaSummary(t.getRuta().getId(), t.getRuta().getNombre());

        return new TrasladoDTO(
                t.getId(), t.getCodigo(), t.getEstado(),
                t.getFechaCreacion(), t.getFechaInicioTransporte(), t.getFechaEntrega(),
                t.getFechaProgramadaInicio(), t.getFechaProgramadaFin(), t.getFechaUltimoCambioEstado(),
                t.getObservaciones(), cp, cg, rs, tr, rt
        );
    }
}
