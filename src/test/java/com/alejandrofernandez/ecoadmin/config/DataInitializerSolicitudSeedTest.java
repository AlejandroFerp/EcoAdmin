package com.alejandrofernandez.ecoadmin.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;

import com.alejandrofernandez.ecoadmin.modelo.SolicitudRegistro;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoSolicitud;
import com.alejandrofernandez.ecoadmin.repository.SolicitudRegistroRepository;
import com.alejandrofernandez.ecoadmin.repository.UsuarioRepository;

@SpringBootTest(properties = {"ecoadmin.seed.enabled=true", "spring.datasource.url=jdbc:h2:mem:seedsoldb;DB_CLOSE_DELAY=-1"})
class DataInitializerSolicitudSeedTest {

    @Autowired
    private DataInitializer dataInitializer;

    @Autowired
    private SolicitudRegistroRepository solicitudRegistroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void clearSolicitudes() {
        solicitudRegistroRepository.deleteAll();
    }

    @Test
    @DisplayName("Repuebla solicitudes demo cuando no existen y evita duplicados")
    void repueblaSolicitudesDemoCuandoNoExisten() throws Exception {
        assertThat(usuarioRepository.count()).isGreaterThan(0);
        assertThat(solicitudRegistroRepository.count()).isZero();

        dataInitializer.run(new DefaultApplicationArguments(new String[0]));

        List<SolicitudRegistro> solicitudes = solicitudRegistroRepository.findAll();
        assertThat(solicitudes).hasSize(4);
        assertThat(solicitudes)
                .extracting(SolicitudRegistro::getEstado)
                .containsExactlyInAnyOrder(
                        EstadoSolicitud.PENDIENTE,
                        EstadoSolicitud.PENDIENTE,
                        EstadoSolicitud.APROBADA,
                        EstadoSolicitud.RECHAZADA);
        assertThat(solicitudes)
                .extracting(SolicitudRegistro::getEmail)
                .containsExactlyInAnyOrder(
                        "lucia.morales.demo@ecoadmin.com",
                        "sergio.vera.demo@ecoadmin.com",
                        "marta.iborra.demo@ecoadmin.com",
                        "raul.pastor.demo@ecoadmin.com");

        dataInitializer.run(new DefaultApplicationArguments(new String[0]));

        assertThat(solicitudRegistroRepository.count()).isEqualTo(4);
    }
}