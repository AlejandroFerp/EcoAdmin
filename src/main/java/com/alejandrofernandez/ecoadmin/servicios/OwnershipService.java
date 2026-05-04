package com.alejandrofernandez.ecoadmin.servicios;

import java.util.List;

import org.springframework.stereotype.Service;

import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.GestorCentro;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;
import com.alejandrofernandez.ecoadmin.repository.CentroRepository;
import com.alejandrofernandez.ecoadmin.repository.GestorCentroRepository;
import com.alejandrofernandez.ecoadmin.repository.RecogidaRepository;
import com.alejandrofernandez.ecoadmin.repository.TrasladoRepository;

/**
 * Servicio central de ownership: dado un usuario y su rol, determina qué
 * centros, traslados y recogidas le pertenecen.
 */
@Service
public class OwnershipService {

    private final CentroRepository centroRepo;
    private final GestorCentroRepository gestorCentroRepo;
    private final TrasladoRepository trasladoRepo;
    private final RecogidaRepository recogidaRepo;

    public OwnershipService(CentroRepository centroRepo,
                            GestorCentroRepository gestorCentroRepo,
                            TrasladoRepository trasladoRepo,
                            RecogidaRepository recogidaRepo) {
        this.centroRepo = centroRepo;
        this.gestorCentroRepo = gestorCentroRepo;
        this.trasladoRepo = trasladoRepo;
        this.recogidaRepo = recogidaRepo;
    }

    /**
     * Devuelve los centros a los que el usuario tiene acceso.
     * - ADMIN: todos
     * - GESTOR: los asignados via GestorCentro
     * - PRODUCTOR: los que posee (Centro.usuario)
     * - TRANSPORTISTA: centros de sus traslados/recogidas (no listado libre)
     */
    public List<Centro> getCentrosPermitidos(Usuario usuario) {
        return switch (usuario.getRol()) {
            case ADMIN -> centroRepo.findAll();
            case GESTOR -> {
                var asignados = gestorCentroRepo.findByGestor(usuario).stream()
                        .map(GestorCentro::getCentro).toList();
                var propios = centroRepo.findByUsuario(usuario);
                yield java.util.stream.Stream.concat(asignados.stream(), propios.stream())
                        .distinct().toList();
            }
            case PRODUCTOR -> centroRepo.findByUsuario(usuario);
            case TRANSPORTISTA -> getCentrosDeOperacionesTransportista(usuario);
        };
    }

    /**
     * ¿Puede el usuario acceder (ver) a este centro?
     */
    public boolean canAccessCentro(Usuario usuario, Long centroId) {
        if (usuario.getRol() == Rol.ADMIN) return true;
        return getCentrosPermitidos(usuario).stream()
                .anyMatch(c -> c.getId().equals(centroId));
    }

    /**
     * ¿Puede el usuario crear recogidas DESDE este centro de origen?
     * - ADMIN/GESTOR: solo si es su centro asignado
     * - PRODUCTOR: solo si es su centro
     * - TRANSPORTISTA: no crea recogidas
     */
    public boolean canCreateRecogidaDesde(Usuario usuario, Long centroOrigenId) {
        return switch (usuario.getRol()) {
            case ADMIN -> true;
            case GESTOR -> canAccessCentro(usuario, centroOrigenId);
            case PRODUCTOR -> canAccessCentro(usuario, centroOrigenId);
            case TRANSPORTISTA -> false;
        };
    }

    /**
     * ¿Puede el usuario enviar recogidas A este centro destino?
     * - ADMIN/GESTOR: siempre (puede enviar a cualquier centro)
     * - PRODUCTOR/TRANSPORTISTA: no envía
     */
    public boolean canSendRecogidaA(Usuario usuario, Long centroDestinoId) {
        return switch (usuario.getRol()) {
            case ADMIN, GESTOR -> true;
            case PRODUCTOR, TRANSPORTISTA -> false;
        };
    }

    /**
     * Devuelve los IDs de centros del transportista (extraídos de traslados y recogidas asignadas).
     */
    private List<Centro> getCentrosDeOperacionesTransportista(Usuario transportista) {
        var centrosTraslados = trasladoRepo.findByTransportista(transportista).stream()
                .flatMap(t -> java.util.stream.Stream.of(t.getCentroProductor(), t.getCentroGestor()));
        var centrosRecogidas = recogidaRepo.findByTransportista(transportista).stream()
                .flatMap(r -> java.util.stream.Stream.of(r.getCentroOrigen(), r.getCentroDestino()))
                .filter(java.util.Objects::nonNull);
        return java.util.stream.Stream.concat(centrosTraslados, centrosRecogidas)
                .distinct().toList();
    }
}
