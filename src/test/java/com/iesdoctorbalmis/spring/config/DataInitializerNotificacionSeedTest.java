package com.iesdoctorbalmis.spring.config;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;

import com.iesdoctorbalmis.spring.modelo.Notificacion;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.repository.NotificacionRepository;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;

@SpringBootTest(properties = "ecoadmin.seed.enabled=true")
class DataInitializerNotificacionSeedTest {

    @Autowired
    private DataInitializer dataInitializer;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${ecoadmin.admin.email:admin@ecoadmin.com}")
    private String adminEmail;

    @BeforeEach
    void clearNotificaciones() {
        notificacionRepository.deleteAll();
    }

    @Test
    @DisplayName("No repuebla notificaciones demo cuando el admin ya tuvo notificaciones, aunque esten leidas")
    void noRepueblaNotificacionesDemoSiYaExisten() throws Exception {
        Usuario admin = usuarioRepository.findByEmail(adminEmail).orElseThrow();
        assertThat(notificacionRepository.countByDestinatarioAndLeidaFalse(admin)).isZero();

        dataInitializer.run(new DefaultApplicationArguments(new String[0]));

        List<Notificacion> notificaciones = notificacionRepository.findByDestinatarioAndLeidaFalseOrderByFechaDesc(admin);
        assertThat(notificaciones).hasSize(3);
        assertThat(notificaciones)
                .extracting(Notificacion::getTitulo)
                .containsExactly(
                        "Resumen diario disponible",
                        "Traslado pendiente de seguimiento",
                        "Nueva solicitud pendiente");

        notificaciones.forEach(notificacion -> notificacion.setLeida(true));
        notificacionRepository.saveAll(notificaciones);

        dataInitializer.run(new DefaultApplicationArguments(new String[0]));

        assertThat(notificacionRepository.findByDestinatarioOrderByFechaDesc(admin)).hasSize(3);
        assertThat(notificacionRepository.countByDestinatarioAndLeidaFalse(admin)).isZero();
    }
}