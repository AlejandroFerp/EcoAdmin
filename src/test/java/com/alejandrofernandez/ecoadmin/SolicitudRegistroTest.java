package com.alejandrofernandez.ecoadmin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.alejandrofernandez.ecoadmin.modelo.SolicitudRegistro;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoSolicitud;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;
import com.alejandrofernandez.ecoadmin.repository.SolicitudRegistroRepository;
import com.alejandrofernandez.ecoadmin.repository.UsuarioRepository;
import com.alejandrofernandez.ecoadmin.repository.NotificacionRepository;

@SpringBootTest
@Transactional
class SolicitudRegistroTest {

    @Autowired WebApplicationContext wac;
    @Autowired SolicitudRegistroRepository solicitudRepo;
    @Autowired UsuarioRepository usuarioRepo;
    @Autowired NotificacionRepository notificacionRepo;
    @Autowired PasswordEncoder passwordEncoder;

    MockMvc mockMvc;
    Usuario admin;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
        admin = usuarioRepo.save(new Usuario("Admin Test", "admin-sol@test.com",
                passwordEncoder.encode("pass"), Rol.ADMIN));
    }

    @Test
    @DisplayName("Crear solicitud publica (sin autenticar)")
    void crearSolicitudPublica() throws Exception {
        mockMvc.perform(post("/api/solicitudes-registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Juan\",\"email\":\"juan@test.com\",\"rolSolicitado\":\"PRODUCTOR\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    @DisplayName("No permite solicitar rol ADMIN")
    void noPermiteRolAdmin() throws Exception {
        mockMvc.perform(post("/api/solicitudes-registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Hacker\",\"email\":\"hacker@test.com\",\"rolSolicitado\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("No permite email duplicado")
    void noPermiteEmailDuplicado() throws Exception {
        // Primero crear usuario con ese email
        usuarioRepo.save(new Usuario("Existente", "existe@test.com",
                passwordEncoder.encode("pass"), Rol.PRODUCTOR));
        mockMvc.perform(post("/api/solicitudes-registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Otro\",\"email\":\"existe@test.com\",\"rolSolicitado\":\"PRODUCTOR\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Listar solicitudes requiere ADMIN")
    void listarRequiereAdmin() throws Exception {
        mockMvc.perform(get("/api/solicitudes-registro")
                .with(user("prod@test.com").roles("PRODUCTOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin puede listar solicitudes")
    void adminListaSolicitudes() throws Exception {
        mockMvc.perform(get("/api/solicitudes-registro")
                .with(user(admin.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin puede aprobar solicitud y crea usuario")
    void aprobarCreaUsuario() throws Exception {
        SolicitudRegistro sol = new SolicitudRegistro();
        sol.setNombre("Maria");
        sol.setEmail("maria@test.com");
        sol.setRolSolicitado(Rol.GESTOR);
        sol = solicitudRepo.save(sol);

        mockMvc.perform(post("/api/solicitudes-registro/" + sol.getId() + "/aprobar")
                .with(user(admin.getEmail()).roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"maria123\"}"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // Verificar que la solicitud fue aprobada
        SolicitudRegistro updated = solicitudRepo.findById(sol.getId()).orElseThrow();
        assert updated.getEstado() == EstadoSolicitud.APROBADA;
        Usuario creado = usuarioRepo.findByEmail("maria@test.com").orElseThrow();
        assert creado.getRol() == Rol.GESTOR;
    }

    @Test
    @DisplayName("Admin puede rechazar solicitud")
    void rechazarSolicitud() throws Exception {
        SolicitudRegistro sol = new SolicitudRegistro();
        sol.setNombre("Pedro");
        sol.setEmail("pedro@test.com");
        sol.setRolSolicitado(Rol.TRANSPORTISTA);
        sol = solicitudRepo.save(sol);

        mockMvc.perform(post("/api/solicitudes-registro/" + sol.getId() + "/rechazar")
                .with(user(admin.getEmail()).roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\":\"Datos incompletos\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"))
                .andExpect(jsonPath("$.motivoRechazo").value("Datos incompletos"));
    }

    @Test
    @DisplayName("Crear solicitud genera notificacion para admins")
    void solicitudGeneraNotificacion() throws Exception {
        long antes = notificacionRepo.countByDestinatarioAndLeidaFalse(admin);
        mockMvc.perform(post("/api/solicitudes-registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Ana\",\"email\":\"ana@test.com\",\"rolSolicitado\":\"PRODUCTOR\"}"))
                .andExpect(status().isCreated());
        long despues = notificacionRepo.countByDestinatarioAndLeidaFalse(admin);
        assert despues == antes + 1 : "Deberia haber 1 notificacion mas";
    }

    @Test
    @DisplayName("Contrasena corta rechazada en aprobacion")
    void contrasenaCorta() throws Exception {
        SolicitudRegistro sol = new SolicitudRegistro();
        sol.setNombre("Corto");
        sol.setEmail("corto@test.com");
        sol.setRolSolicitado(Rol.PRODUCTOR);
        sol = solicitudRepo.save(sol);

        mockMvc.perform(post("/api/solicitudes-registro/" + sol.getId() + "/aprobar")
                .with(user(admin.getEmail()).roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"ab\"}"))
                .andExpect(status().isBadRequest());
    }
}
