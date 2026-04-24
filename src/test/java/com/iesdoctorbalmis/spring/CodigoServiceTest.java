package com.iesdoctorbalmis.spring;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Year;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.iesdoctorbalmis.spring.modelo.SecuenciaCodigo;
import com.iesdoctorbalmis.spring.repository.SecuenciaCodigoRepository;
import com.iesdoctorbalmis.spring.servicios.CodigoService;

class CodigoServiceTest {

    private SecuenciaCodigoRepository repo;
    private CodigoService service;

    @BeforeEach
    void setUp() {
        repo = mock(SecuenciaCodigoRepository.class);
        service = new CodigoService(repo);
    }

    @Test
    void generaFormatoCorrecto() {
        var seq = new SecuenciaCodigo();
        when(repo.findByClaveConBloqueo(any())).thenReturn(Optional.of(seq));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        String codigo = service.generar("TRA");

        int anio = Year.now().getValue() % 100;
        assertTrue(codigo.startsWith("TRA" + String.format("%02d", anio) + "-"),
                "Debe empezar por TRA{AA}-");
        assertEquals(10, codigo.length(), "Longitud esperada: TTRAAA-000001 = 10 chars");
        assertTrue(codigo.matches("[A-Z]{3}\\d{2}-\\d{6}"), "Formato incorrecto: " + codigo);
    }

    @Test
    void dosLlamadasGeneranNumeroDistinto() {
        var seq = new SecuenciaCodigo();
        when(repo.findByClaveConBloqueo(any())).thenReturn(Optional.of(seq));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        String primero  = service.generar("CEN");
        String segundo  = service.generar("CEN");

        assertNotEquals(primero, segundo, "Dos llamadas deben producir codigos distintos");
    }

    @Test
    void prefijosDistintosSecuenciasIndependientes() {
        var seqTra = new SecuenciaCodigo();
        var seqCen = new SecuenciaCodigo();

        when(repo.findByClaveConBloqueo(argThat(k -> k != null && k.startsWith("TRA"))))
                .thenReturn(Optional.of(seqTra));
        when(repo.findByClaveConBloqueo(argThat(k -> k != null && k.startsWith("CEN"))))
                .thenReturn(Optional.of(seqCen));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        String tra = service.generar("TRA");
        String cen = service.generar("CEN");

        // Ambos deben ser el primer número porque tienen secuencias distintas
        int anio = Year.now().getValue() % 100;
        assertEquals("TRA" + String.format("%02d", anio) + "-000001", tra);
        assertEquals("CEN" + String.format("%02d", anio) + "-000001", cen);
    }

    @Test
    void anioEnCodigo() {
        var seq = new SecuenciaCodigo();
        when(repo.findByClaveConBloqueo(any())).thenReturn(Optional.of(seq));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        String codigo = service.generar("RES");

        int anioEsperado = Year.now().getValue() % 100;
        String parteAnio = codigo.substring(3, 5); // chars 3-4 = "26"
        assertEquals(String.format("%02d", anioEsperado), parteAnio,
                "El año debe ser los últimos dos dígitos del año actual");
    }

    @Test
    void creaSecuenciaSiNoExiste() {
        when(repo.findByClaveConBloqueo(any())).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> service.generar("DOC"),
                "Debe crear la secuencia automáticamente si no existe");
        verify(repo, times(1)).save(any(SecuenciaCodigo.class));
    }
}
