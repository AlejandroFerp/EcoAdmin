package com.alejandrofernandez.ecoadmin;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.alejandrofernandez.ecoadmin.modelo.Notificacion;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;
import com.alejandrofernandez.ecoadmin.repository.NotificacionRepository;
import com.alejandrofernandez.ecoadmin.repository.UsuarioRepository;

@SpringBootTest
@Transactional
class NotificacionControllerTest {

    @Autowired WebApplicationContext wac;
    @Autowired NotificacionRepository notificacionRepo;
    @Autowired UsuarioRepository usuarioRepo;
    @Autowired PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private Usuario admin;
    private Usuario otroAdmin;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
        admin = usuarioRepo.save(new Usuario("Admin Notif", "admin-notif@test.com",
                passwordEncoder.encode("pass"), Rol.ADMIN));
        otroAdmin = usuarioRepo.save(new Usuario("Otro Admin", "otro-admin-notif@test.com",
                passwordEncoder.encode("pass"), Rol.ADMIN));
    }

    @Test
    @DisplayName("Listar notificaciones devuelve solo las no leidas del usuario autenticado")
    void listarSoloNoLeidas() throws Exception {
        Notificacion pendiente = crearNotificacion(admin, "Nueva solicitud", false, "/solicitudes/1");
        crearNotificacion(admin, "Solicitud revisada", true, "/solicitudes/2");
        crearNotificacion(otroAdmin, "Otra notificacion", false, "/solicitudes/3");

        mockMvc.perform(get("/api/notificaciones")
                .with(user(admin.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(pendiente.getId()))
                .andExpect(jsonPath("$[0].titulo").value("Nueva solicitud"))
                .andExpect(jsonPath("$[0].leida").value(false));
    }

    @Test
    @DisplayName("Contador de no leidas devuelve el total numerico")
    void contadorNoLeidasDevuelveTotal() throws Exception {
        crearNotificacion(admin, "Pendiente 1", false, "/solicitudes/1");
        crearNotificacion(admin, "Pendiente 2", false, "/solicitudes/2");
        crearNotificacion(admin, "Leida", true, "/solicitudes/3");

        mockMvc.perform(get("/api/notificaciones/no-leidas")
                .with(user(admin.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    @DisplayName("Leer todas marca solo las notificaciones pendientes del usuario autenticado")
    void leerTodasMarcaPendientesDelUsuario() throws Exception {
        crearNotificacion(admin, "Pendiente 1", false, "/solicitudes/1");
        crearNotificacion(admin, "Pendiente 2", false, "/solicitudes/2");
        crearNotificacion(otroAdmin, "Pendiente ajena", false, "/solicitudes/3");

        mockMvc.perform(patch("/api/notificaciones/leer-todas")
                .with(user(admin.getEmail()).roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertThat(notificacionRepo.countByDestinatarioAndLeidaFalse(admin)).isZero();
        assertThat(notificacionRepo.countByDestinatarioAndLeidaFalse(otroAdmin)).isEqualTo(1);
    }

    private Notificacion crearNotificacion(Usuario destinatario, String titulo, boolean leida, String enlace) {
        Notificacion notificacion = new Notificacion(destinatario, titulo, "Mensaje de " + titulo, enlace);
        notificacion.setLeida(leida);
        return notificacionRepo.save(notificacion);
    }
}