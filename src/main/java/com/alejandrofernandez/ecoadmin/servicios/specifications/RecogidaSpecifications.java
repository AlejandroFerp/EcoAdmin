package com.alejandrofernandez.ecoadmin.servicios.specifications;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.alejandrofernandez.ecoadmin.modelo.Recogida;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;

/**
 * Specifications JPA para filtrar recogidas según el ownership del usuario.
 */
public final class RecogidaSpecifications {

    private RecogidaSpecifications() {}

    /**
     * ADMIN → sin filtro.
     * GESTOR → recogidas donde centroOrigen o centroDestino es de sus centros.
     * PRODUCTOR → recogidas donde centroOrigen es su centro.
     * TRANSPORTISTA → recogidas donde es el transportista asignado.
     */
    public static Specification<Recogida> deUsuario(Usuario usuario, List<Long> centroIdsPermitidos) {
        if (usuario.getRol() == Rol.ADMIN) {
            return (root, query, cb) -> cb.conjunction();
        }
        if (usuario.getRol() == Rol.TRANSPORTISTA) {
            return (root, query, cb) -> cb.equal(root.get("transportista"), usuario);
        }
        if (usuario.getRol() == Rol.PRODUCTOR) {
            return (root, query, cb) -> root.get("centroOrigen").get("id").in(centroIdsPermitidos);
        }
        // GESTOR: ve recogidas donde centroOrigen o centroDestino está en sus centros
        return (root, query, cb) -> cb.or(
                root.get("centroOrigen").get("id").in(centroIdsPermitidos),
                root.get("centroDestino").get("id").in(centroIdsPermitidos)
        );
    }
}
