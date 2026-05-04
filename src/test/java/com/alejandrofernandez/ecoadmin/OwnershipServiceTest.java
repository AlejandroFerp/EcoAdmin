package com.alejandrofernandez.ecoadmin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.GestorCentro;
import com.alejandrofernandez.ecoadmin.modelo.Recogida;
import com.alejandrofernandez.ecoadmin.modelo.Traslado;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;
import com.alejandrofernandez.ecoadmin.repository.CentroRepository;
import com.alejandrofernandez.ecoadmin.repository.GestorCentroRepository;
import com.alejandrofernandez.ecoadmin.repository.RecogidaRepository;
import com.alejandrofernandez.ecoadmin.repository.TrasladoRepository;
import com.alejandrofernandez.ecoadmin.servicios.OwnershipService;

class OwnershipServiceTest {

    private CentroRepository centroRepo;
    private GestorCentroRepository gestorCentroRepo;
    private TrasladoRepository trasladoRepo;
    private RecogidaRepository recogidaRepo;
    private OwnershipService service;

    private Usuario admin;
    private Usuario gestor;
    private Usuario productor;
    private Usuario transportista;
    private Centro centro1;
    private Centro centro2;
    private Centro centro3;

    @BeforeEach
    void setUp() {
        centroRepo = mock(CentroRepository.class);
        gestorCentroRepo = mock(GestorCentroRepository.class);
        trasladoRepo = mock(TrasladoRepository.class);
        recogidaRepo = mock(RecogidaRepository.class);
        service = new OwnershipService(centroRepo, gestorCentroRepo, trasladoRepo, recogidaRepo);

        admin = new Usuario("Admin", "admin@test.com", "pass", Rol.ADMIN);
        admin.setId(1L);
        gestor = new Usuario("Gestor", "gestor@test.com", "pass", Rol.GESTOR);
        gestor.setId(2L);
        productor = new Usuario("Productor", "productor@test.com", "pass", Rol.PRODUCTOR);
        productor.setId(3L);
        transportista = new Usuario("Trans", "trans@test.com", "pass", Rol.TRANSPORTISTA);
        transportista.setId(4L);

        centro1 = new Centro();
        centro1.setId(10L);
        centro2 = new Centro();
        centro2.setId(20L);
        centro3 = new Centro();
        centro3.setId(30L);
    }

    @Test
    @DisplayName("Admin ve todos los centros")
    void adminVeTodo() {
        when(centroRepo.findAll()).thenReturn(List.of(centro1, centro2, centro3));
        var result = service.getCentrosPermitidos(admin);
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("Gestor ve centros asignados via GestorCentro y centros propios")
    void gestorVeSusCentros() {
        var gc = new GestorCentro(gestor, centro1);
        when(gestorCentroRepo.findByGestor(gestor)).thenReturn(List.of(gc));
        when(centroRepo.findByUsuario(gestor)).thenReturn(List.of(centro2));
        var result = service.getCentrosPermitidos(gestor);
        assertEquals(2, result.size());
        assertTrue(result.contains(centro1));
        assertTrue(result.contains(centro2));
    }

    @Test
    @DisplayName("Productor solo ve centros donde es usuario propietario")
    void productorVeSusCentros() {
        when(centroRepo.findByUsuario(productor)).thenReturn(List.of(centro2));
        var result = service.getCentrosPermitidos(productor);
        assertEquals(1, result.size());
        assertEquals(centro2.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Transportista ve centros de sus traslados y recogidas")
    void transportistaVeCentrosDeOperaciones() {
        var traslado = new Traslado(centro1, centro2, null, transportista);
        when(trasladoRepo.findByTransportista(transportista)).thenReturn(List.of(traslado));
        when(recogidaRepo.findByTransportista(transportista)).thenReturn(List.of());
        var result = service.getCentrosPermitidos(transportista);
        assertEquals(2, result.size());
        assertTrue(result.contains(centro1));
        assertTrue(result.contains(centro2));
    }

    @Test
    @DisplayName("canAccessCentro: admin siempre true")
    void canAccessAdminSiempre() {
        assertTrue(service.canAccessCentro(admin, 999L));
    }

    @Test
    @DisplayName("canAccessCentro: productor solo su centro")
    void canAccessProductorSoloPropios() {
        when(centroRepo.findByUsuario(productor)).thenReturn(List.of(centro2));
        assertTrue(service.canAccessCentro(productor, 20L));
        assertFalse(service.canAccessCentro(productor, 10L));
    }

    @Test
    @DisplayName("canCreateRecogidaDesde: transportista no puede")
    void transportistaNoCreaRecogidas() {
        assertFalse(service.canCreateRecogidaDesde(transportista, 10L));
    }

    @Test
    @DisplayName("canSendRecogidaA: gestor puede enviar a cualquier centro")
    void gestorEnviaACualquierCentro() {
        assertTrue(service.canSendRecogidaA(gestor, 999L));
    }

    @Test
    @DisplayName("canSendRecogidaA: productor no puede enviar")
    void productorNoEnvia() {
        assertFalse(service.canSendRecogidaA(productor, 10L));
    }
}
