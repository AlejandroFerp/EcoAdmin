package com.iesdoctorbalmis.spring;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Direccion;
import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.DireccionRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.TrasladoRepository;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;

@SpringBootTest
@Transactional
class TrasladoEndpointTest {

    private MockMvc mockMvc;
    @Autowired private WebApplicationContext context;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private CentroRepository centroRepo;
    @Autowired private ResiduoRepository residuoRepo;
    @Autowired private TrasladoRepository trasladoRepo;
    @Autowired private DireccionRepository direccionRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    private Usuario gestor;
    private Traslado traslado;
    private Direccion dirA;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        gestor = usuarioRepo.save(new Usuario("Gestor", "gestor@test.com",
                passwordEncoder.encode("pass"), Rol.GESTOR));
        Usuario transportista = usuarioRepo.save(new Usuario("Trans", "trans2@test.com",
                passwordEncoder.encode("pass"), Rol.TRANSPORTISTA));

        dirA = direccionRepo.save(new Direccion("C/ A", "Alicante", "03001", "Alicante", "Espana"));
        Direccion dirB = direccionRepo.save(new Direccion("C/ B", "Valencia", "46001", "Valencia", "Espana"));

        Centro cp = centroRepo.save(new Centro(gestor, "Productor SL", "PRODUCTOR", dirA));
        Centro cg = centroRepo.save(new Centro("Gestor SL", "GESTOR", dirB));
        Residuo r = residuoRepo.save(new Residuo(200.0, "litros", "PENDIENTE", cp));
        traslado = trasladoRepo.save(new Traslado(cp, cg, r, transportista));
    }

    @Test
    @DisplayName("GET /api/traslados/{id} devuelve 404 para ID inexistente")
    void buscar_noExiste_404() throws Exception {
        mockMvc.perform(get("/api/traslados/99999")
                .with(user(gestor.getEmail()).roles("GESTOR")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/traslados/{id} devuelve traslado existente")
    void buscar_existe_200() throws Exception {
        mockMvc.perform(get("/api/traslados/" + traslado.getId())
                .with(user(gestor.getEmail()).roles("GESTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(traslado.getId()))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("PATCH estado: transicion valida PENDIENTE -> EN_TRANSITO")
    void cambiarEstado_valida() throws Exception {
        mockMvc.perform(patch("/api/traslados/" + traslado.getId() + "/estado")
                .param("estado", "EN_TRANSITO")
                .param("comentario", "Inicio recogida")
                .with(user(gestor.getEmail()).roles("GESTOR"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_TRANSITO"));
    }

    @Test
    @DisplayName("PATCH estado: transicion invalida al mismo estado devuelve 400")
    void cambiarEstado_invalida_400() throws Exception {
        // Con libertad total, lo unico no permitido es transicionar al mismo estado
        // (genera evento vacio en historial sin aportar informacion)
        mockMvc.perform(patch("/api/traslados/" + traslado.getId() + "/estado")
                .param("estado", "PENDIENTE")
                .with(user(gestor.getEmail()).roles("GESTOR"))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH estado: ciclo completo via API")
    void cambiarEstado_cicloCompleto() throws Exception {
        mockMvc.perform(patch("/api/traslados/" + traslado.getId() + "/estado")
                .param("estado", "EN_TRANSITO")
                .with(user(gestor.getEmail()).roles("GESTOR")).with(csrf()))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/traslados/" + traslado.getId() + "/estado")
                .param("estado", "ENTREGADO")
                .with(user(gestor.getEmail()).roles("GESTOR")).with(csrf()))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/traslados/" + traslado.getId() + "/estado")
                .param("estado", "COMPLETADO")
                .with(user(gestor.getEmail()).roles("GESTOR")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADO"));
    }

    @Test
    @DisplayName("GET /api/traslados/{id}/historial devuelve eventos")
    void historial_devuelveEventos() throws Exception {
        mockMvc.perform(patch("/api/traslados/" + traslado.getId() + "/estado")
                .param("estado", "EN_TRANSITO").param("comentario", "Sale")
                .with(user(gestor.getEmail()).roles("GESTOR")).with(csrf()));
        mockMvc.perform(patch("/api/traslados/" + traslado.getId() + "/estado")
                .param("estado", "ENTREGADO").param("comentario", "Llega")
                .with(user(gestor.getEmail()).roles("GESTOR")).with(csrf()));
        mockMvc.perform(get("/api/traslados/" + traslado.getId() + "/historial")
                .with(user(gestor.getEmail()).roles("GESTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/estadisticas devuelve datos")
    void estadisticas() throws Exception {
        mockMvc.perform(get("/api/estadisticas")
                .with(user(gestor.getEmail()).roles("GESTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCentros").isNumber())
                .andExpect(jsonPath("$.trasladosPendientes").isNumber());
    }

    @Test
    @DisplayName("POST /api/centros crea centro y devuelve 201")
    void crearCentro() throws Exception {
        String json = "{\"nombre\":\"Nuevo Centro\",\"tipo\":\"PRODUCTOR\",\"direccion\":{\"id\":" + dirA.getId() + "}}";
        mockMvc.perform(post("/api/centros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(user(gestor.getEmail()).roles("GESTOR"))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Nuevo Centro"));
    }
}

