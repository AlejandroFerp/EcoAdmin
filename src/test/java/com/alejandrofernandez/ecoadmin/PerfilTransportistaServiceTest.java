package com.alejandrofernandez.ecoadmin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.alejandrofernandez.ecoadmin.excepciones.RecursoNoEncontradoException;
import com.alejandrofernandez.ecoadmin.modelo.PerfilTransportista;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;
import com.alejandrofernandez.ecoadmin.repository.PerfilTransportistaRepository;
import com.alejandrofernandez.ecoadmin.repository.UsuarioRepository;
import com.alejandrofernandez.ecoadmin.servicios.PerfilTransportistaService;
import com.alejandrofernandez.ecoadmin.servicios.TarifaValidator;
import com.alejandrofernandez.ecoadmin.servicios.TarifaValidator.ResultadoValidacion;

class PerfilTransportistaServiceTest {

    private PerfilTransportistaRepository repo;
    private UsuarioRepository usuarioRepo;
    private TarifaValidator validator;
    private PerfilTransportistaService service;

    @BeforeEach
    void setUp() {
        repo = mock(PerfilTransportistaRepository.class);
        usuarioRepo = mock(UsuarioRepository.class);
        validator = mock(TarifaValidator.class);
        service = new PerfilTransportistaService(repo, usuarioRepo, validator);
    }

    private Usuario transportista() {
        Usuario u = new Usuario();
        u.setRol(Rol.TRANSPORTISTA);
        return u;
    }

    @Test
    @DisplayName("guardar con usuario inexistente lanza excepcion")
    void guardar_usuarioInexistente_lanzaExcepcion() {
        when(usuarioRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
            () -> service.guardar(99L, new PerfilTransportista()));
    }

    @Test
    @DisplayName("guardar con rol no transportista lanza excepcion")
    void guardar_rolNoTransportista_lanzaExcepcion() {
        Usuario u = new Usuario();
        u.setRol(Rol.GESTOR);
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(u));

        assertThrows(IllegalArgumentException.class,
            () -> service.guardar(1L, new PerfilTransportista()));
    }

    @Test
    @DisplayName("guardar con formula invalida lanza excepcion")
    void guardar_formulaInvalida_lanzaExcepcion() {
        Usuario u = transportista();
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(u));
        when(validator.validar("XXX")).thenReturn(new ResultadoValidacion(false, "Formula invalida"));

        PerfilTransportista datos = new PerfilTransportista();
        datos.setFormulaTarifa("XXX");

        assertThrows(IllegalArgumentException.class, () -> service.guardar(1L, datos));
    }

    @Test
    @DisplayName("guardar crea perfil nuevo si no existe")
    void guardar_perfilNuevo_creaCorrecto() {
        Usuario u = transportista();
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(u));
        when(repo.findByUsuario(u)).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        PerfilTransportista datos = new PerfilTransportista();
        datos.setMatricula("1234-ABC");
        datos.setObservaciones("Test");

        PerfilTransportista result = service.guardar(1L, datos);
        assertEquals("1234-ABC", result.getMatricula());
        assertEquals("Test", result.getObservaciones());
        assertEquals(u, result.getUsuario());
    }

    @Test
    @DisplayName("guardar actualiza perfil existente")
    void guardar_perfilExistente_actualiza() {
        Usuario u = transportista();
        PerfilTransportista existente = new PerfilTransportista();
        existente.setMatricula("OLD");
        existente.setUsuario(u);

        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(u));
        when(repo.findByUsuario(u)).thenReturn(Optional.of(existente));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        PerfilTransportista datos = new PerfilTransportista();
        datos.setMatricula("NEW-123");

        PerfilTransportista result = service.guardar(1L, datos);
        assertEquals("NEW-123", result.getMatricula());
    }

    @Test
    @DisplayName("findByUsuarioId con usuario inexistente devuelve vacio")
    void findByUsuarioId_inexistente_devuelveVacio() {
        when(usuarioRepo.findById(99L)).thenReturn(Optional.empty());
        assertTrue(service.findByUsuarioId(99L).isEmpty());
    }
}
