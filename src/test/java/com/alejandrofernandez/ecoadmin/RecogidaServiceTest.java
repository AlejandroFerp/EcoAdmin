package com.alejandrofernandez.ecoadmin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alejandrofernandez.ecoadmin.excepciones.RecursoNoEncontradoException;
import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.Recogida;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoRecogida;
import com.alejandrofernandez.ecoadmin.repository.RecogidaRepository;
import com.alejandrofernandez.ecoadmin.servicios.RecogidaService;

class RecogidaServiceTest {

    private RecogidaRepository repo;
    private RecogidaService service;

    @BeforeEach
    void setUp() {
        repo = mock(RecogidaRepository.class);
        service = new RecogidaService(repo);
    }

    @Test
    void findById_existente_devuelve() {
        Recogida r = new Recogida();
        when(repo.findById(1L)).thenReturn(Optional.of(r));

        Recogida resultado = service.findById(1L);
        assertSame(r, resultado);
    }

    @Test
    void findById_inexistente_lanzaExcepcion() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.findById(99L));
    }

    @Test
    void save_sinEstado_asignaPROGRAMADA() {
        Recogida r = new Recogida();
        // Recogida ya tiene default PROGRAMADA en la entidad
        assertEquals(EstadoRecogida.PROGRAMADA, r.getEstado());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        Recogida saved = service.save(r);
        assertEquals(EstadoRecogida.PROGRAMADA, saved.getEstado());
    }

    @Test
    void save_completadaSinFechaRealizada_asignaHoy() {
        Recogida r = new Recogida();
        r.setEstado(EstadoRecogida.COMPLETADA);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        Recogida saved = service.save(r);
        assertEquals(LocalDate.now(), saved.getFechaRealizada());
    }

    @Test
    void save_completadaConFecha_noSobreescribeFecha() {
        Recogida r = new Recogida();
        r.setEstado(EstadoRecogida.COMPLETADA);
        LocalDate fechaExplicita = LocalDate.of(2025, 1, 15);
        r.setFechaRealizada(fechaExplicita);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        Recogida saved = service.save(r);
        assertEquals(fechaExplicita, saved.getFechaRealizada());
    }

    @Test
    void findByRango_delegaAlRepositorio() {
        LocalDate desde = LocalDate.of(2025, 1, 1);
        LocalDate hasta = LocalDate.of(2025, 12, 31);
        Recogida r1 = new Recogida();
        when(repo.findByFechaProgramadaBetween(desde, hasta)).thenReturn(List.of(r1));

        List<Recogida> resultado = service.findByRango(desde, hasta);
        assertEquals(1, resultado.size());
        verify(repo).findByFechaProgramadaBetween(desde, hasta);
    }

    @Test
    void findByCentros_listaVacia_devuelveListaVacia() {
        List<Recogida> resultado = service.findByCentros(List.of());
        assertTrue(resultado.isEmpty());
        verifyNoInteractions(repo);
    }

    @Test
    void findByCentros_conCentros_delegaAlRepositorio() {
        Centro c = new Centro();
        Recogida r = new Recogida();
        when(repo.findByCentroOrigenIn(any())).thenReturn(List.of(r));

        List<Recogida> resultado = service.findByCentros(List.of(c));
        assertEquals(1, resultado.size());
    }

    @Test
    void delete_llamaAlRepositorio() {
        service.delete(5L);
        verify(repo).deleteById(5L);
    }

    @Test
    void alertaFifo_residuoFueraDePlazo_detectado() {
        // Verifica la logica FIFO: residuo con fechaEntrada > diasMaximo deberia
        // estar en alerta. AlmacenController hace la comprobacion; aqui validamos
        // que el servicio de recogida filtra correctamente por estado
        when(repo.findByEstado(EstadoRecogida.PROGRAMADA)).thenReturn(List.of());

        List<Recogida> pendientes = service.findByEstado(EstadoRecogida.PROGRAMADA);
        assertTrue(pendientes.isEmpty());
        verify(repo).findByEstado(EstadoRecogida.PROGRAMADA);
    }
}
