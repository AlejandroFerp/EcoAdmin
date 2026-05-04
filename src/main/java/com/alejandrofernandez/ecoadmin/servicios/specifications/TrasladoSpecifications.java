package com.alejandrofernandez.ecoadmin.servicios.specifications;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.alejandrofernandez.ecoadmin.modelo.Traslado;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;

/**
 * Specifications JPA para filtrar traslados según el ownership del usuario.
 */
public final class TrasladoSpecifications {

    private TrasladoSpecifications() {}

    /**
     * ADMIN → sin filtro.
     * GESTOR → traslados donde centroProductor o centroGestor es de sus centros.
     * PRODUCTOR → traslados donde centroProductor es su centro.
     * TRANSPORTISTA → traslados donde es el transportista asignado.
     */
    public static Specification<Traslado> deUsuario(Usuario usuario, List<Long> centroIdsPermitidos) {
        if (usuario.getRol() == Rol.ADMIN) {
            return (root, query, cb) -> cb.conjunction();
        }
        if (usuario.getRol() == Rol.TRANSPORTISTA) {
            return (root, query, cb) -> cb.equal(root.get("transportista"), usuario);
        }
        if (usuario.getRol() == Rol.PRODUCTOR) {
            return (root, query, cb) -> root.get("centroProductor").get("id").in(centroIdsPermitidos);
        }
        // GESTOR: ve traslados donde centroProductor o centroGestor está en sus centros
        return (root, query, cb) -> cb.or(
                root.get("centroProductor").get("id").in(centroIdsPermitidos),
                root.get("centroGestor").get("id").in(centroIdsPermitidos)
        );
    }
}
