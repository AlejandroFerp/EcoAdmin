package com.iesdoctorbalmis.spring;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.TrasladoRepository;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;

@SpringBootTest
@Transactional
class AccesoControllerTest {

    private MockMvc mockMvc;
    @Autowired private WebApplicationContext context;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private CentroRepository centroRepo;
    @Autowired private ResiduoRepository residuoRepo;
    @Autowired private TrasladoRepository trasladoRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    private Usuario admin;
    private Usuario productor;
    private Usuario otroProductor;
    private Usuario transportista;
    private Centro centroDelProductor;
    private Centro centroDeOtro;
    private Traslado trasladoDelProductor;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        admin = usuarioRepo.save(new Usuario("Admin", "admin-test@test.com",
                passwordEncoder.encode("pass"), Rol.ADMIN));
        productor = usuarioRepo.save(new Usuario("Productor", "prod@test.com",
                passwordEncoder.encode("pass"), Rol.PRODUCTOR));
        otroProductor = usuarioRepo.save(new Usuario("Otro", "otro@test.com",
                passwordEncoder.encode("pass"), Rol.PRODUCTOR));
        transportista = usuarioRepo.save(new Usuario("Trans", "trans@test.com",
                passwordEncoder.encode("pass"), Rol.TRANSPORTISTA));

        centroDelProductor = centroRepo.save(
                new Centro(productor, "Mi Centro", "PRODUCTOR", "Calle 1", "Alicante"));
        centroDeOtro = centroRepo.save(
                new Centro(otroProductor, "Su Centro", "PRODUCTOR", "Calle 2", "Madrid"));

        Centro centroGestor = centroRepo.save(
                new Centro("Gestor S.L.", "GESTOR", "Calle 3", "Valencia"));
        Residuo residuo = residuoRepo.save(
                new Residuo(50.0, "kg", "PENDIENTE", centroDelProductor));

        trasladoDelProductor = trasladoRepo.save(
                new Traslado(centroDelProductor, centroGestor, residuo, transportista));
    }

    @Nested
    @DisplayName("Acceso a /api/usuarios (solo ADMIN)")
    class UsuariosAcceso {

        @Test
        @DisplayName("Admin puede listar usuarios")
        void adminListaUsuarios() throws Exception {
            mockMvc.perform(get("/api/usuarios")
                    .with(user(admin.getEmail()).roles("ADMIN")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Productor no puede listar usuarios (403)")
        void productorNoListaUsuarios() throws Exception {
            mockMvc.perform(get("/api/usuarios")
                    .with(user(productor.getEmail()).roles("PRODUCTOR")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Sin autenticacion redirige a login")
        void sinAuthRedirige() throws Exception {
            mockMvc.perform(get("/api/usuarios"))
                    .andExpect(status().is3xxRedirection());
        }
    }

    @Nested
    @DisplayName("Ownership en /api/centros")
    class CentrosOwnership {

        @Test
        @DisplayName("Productor solo ve sus propios centros")
        void productorVeSusCentros() throws Exception {
            mockMvc.perform(get("/api/centros")
                    .with(user(productor.getEmail()).roles("PRODUCTOR")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].nombre").value("Mi Centro"));
        }

        @Test
        @DisplayName("Admin ve todos los centros")
        void adminVeTodosCentros() throws Exception {
            mockMvc.perform(get("/api/centros")
                    .with(user(admin.getEmail()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3));
        }

        @Test
        @DisplayName("Productor no puede acceder a centro ajeno (403)")
        void productorNoPuedeVerCentroAjeno() throws Exception {
            mockMvc.perform(get("/api/centros/" + centroDeOtro.getId())
                    .with(user(productor.getEmail()).roles("PRODUCTOR")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Productor no puede borrar centros (403)")
        void productorNoPuedeBorrar() throws Exception {
            mockMvc.perform(delete("/api/centros/" + centroDelProductor.getId())
                    .with(user(productor.getEmail()).roles("PRODUCTOR")))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Ownership en /api/traslados")
    class TrasladosOwnership {

        @Test
        @DisplayName("Productor solo ve traslados de sus centros")
        void productorVeSusTraslados() throws Exception {
            mockMvc.perform(get("/api/traslados")
                    .with(user(productor.getEmail()).roles("PRODUCTOR")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("Otro productor no ve traslados ajenos")
        void otroProductorNoVeAjenos() throws Exception {
            mockMvc.perform(get("/api/traslados")
                    .with(user(otroProductor.getEmail()).roles("PRODUCTOR")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Transportista ve traslados asignados")
        void transportistaVeAsignados() throws Exception {
            mockMvc.perform(get("/api/traslados")
                    .with(user(transportista.getEmail()).roles("TRANSPORTISTA")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("Productor no puede acceder a traslado ajeno (403)")
        void productorNoAccedeAjeno() throws Exception {
            mockMvc.perform(get("/api/traslados/" + trasladoDelProductor.getId())
                    .with(user(otroProductor.getEmail()).roles("PRODUCTOR")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Productor no puede borrar traslados (403)")
        void productorNoPuedeBorrar() throws Exception {
            mockMvc.perform(delete("/api/traslados/" + trasladoDelProductor.getId())
                    .with(user(productor.getEmail()).roles("PRODUCTOR")))
                    .andExpect(status().isForbidden());
        }
    }
}