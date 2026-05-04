package com.alejandrofernandez.ecoadmin.dto;

import com.alejandrofernandez.ecoadmin.modelo.Centro;

public record CentroDTO(
        Long id,
        String codigo,
        String nombre,
        String tipo,
        String nima,
        String telefono,
        String email,
        String nombreContacto,
        String detalleEnvio,
        UsuarioSummary usuario,
        DireccionDTO direccion
) {
    public record UsuarioSummary(Long id, String nombre, String email) {}

    public record DireccionDTO(
            Long id, String codigo, String calle, String ciudad,
            String codigoPostal, String provincia, String pais,
            Double latitud, Double longitud
    ) {}

    public static CentroDTO from(Centro c) {
        UsuarioSummary us = c.getUsuario() == null ? null
                : new UsuarioSummary(c.getUsuario().getId(),
                        c.getUsuario().getNombre(), c.getUsuario().getEmail());

        DireccionDTO dir = c.getDireccion() == null ? null
                : new DireccionDTO(c.getDireccion().getId(), c.getDireccion().getCodigo(),
                        c.getDireccion().getCalle(), c.getDireccion().getCiudad(),
                        c.getDireccion().getCodigoPostal(), c.getDireccion().getProvincia(),
                        c.getDireccion().getPais(), c.getDireccion().getLatitud(),
                        c.getDireccion().getLongitud());

        return new CentroDTO(
                c.getId(), c.getCodigo(), c.getNombre(), c.getTipo(),
                c.getNima(), c.getTelefono(), c.getEmail(),
                c.getNombreContacto(), c.getDetalleEnvio(), us, dir
        );
    }
}
