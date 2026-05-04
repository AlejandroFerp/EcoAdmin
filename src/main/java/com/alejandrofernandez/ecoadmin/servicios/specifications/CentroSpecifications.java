package com.alejandrofernandez.ecoadmin.servicios.specifications;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;

/**
 * Specifications JPA para filtrar centros según el ownership del usuario.
 */
public final class CentroSpecifications {

    private CentroSpecifications() {}

    /**
     * Devuelve un Specification que filtra centros según el rol y pertenencia del usuario.
     * ADMIN → sin filtro (ve todo).
     * GESTOR → centros asignados via GestorCentro.
     * PRODUCTOR → centros donde Centro.usuario = usuario.
     * TRANSPORTISTA → null (usa OwnershipService.getCentrosPermitidos directamente).
     */
    public static Specification<Centro> deUsuario(Usuario usuario, List<Long> centroIdsPermitidos) {
        if (usuario.getRol() == Rol.ADMIN) {
            return (root, query, cb) -> cb.conjunction();
        }
        if (usuario.getRol() == Rol.PRODUCTOR) {
            return (root, query, cb) -> cb.equal(root.get("usuario"), usuario);
        }
        // GESTOR y TRANSPORTISTA: filtrar por lista de IDs permitidos
        return (root, query, cb) -> root.get("id").in(centroIdsPermitidos);
    }
}
