package com.alejandrofernandez.ecoadmin.servicios.specifications;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.alejandrofernandez.ecoadmin.modelo.Residuo;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;

/**
 * Specifications JPA para filtrar residuos según el ownership del usuario.
 * Los residuos se filtran indirectamente: un residuo pertenece a un centro.
 */
public final class ResiduoSpecifications {

    private ResiduoSpecifications() {}

    /**
     * Filtra residuos cuyo centro está en la lista de centros permitidos para el usuario.
     * ADMIN → sin filtro.
     */
    public static Specification<Residuo> deUsuario(Usuario usuario, List<Long> centroIdsPermitidos) {
        if (usuario.getRol() == Rol.ADMIN) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> root.get("centro").get("id").in(centroIdsPermitidos);
    }
}
