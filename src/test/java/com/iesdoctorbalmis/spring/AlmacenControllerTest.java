package com.iesdoctorbalmis.spring;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Direccion;
import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.DireccionRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;

@SpringBootTest
@Transactional
class AlmacenControllerTest {

    private MockMvc mockMvc;
    @Autowired private WebApplicationContext context;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private CentroRepository centroRepo;
    @Autowired private ResiduoRepository residuoRepo;
    @Autowired private DireccionRepository direccionRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    private Usuario gestor;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        gestor = usuarioRepo.save(new Usuario("Gestor Almacen", "gestor-almacen@test.com",
                passwordEncoder.encode("pass"), Rol.GESTOR));
        Direccion dir = direccionRepo.save(new Direccion("C/ Almacen", "Alicante", "03001", "Alicante", "Espana"));
        Centro c = centroRepo.save(new Centro(gestor, "Centro Almacen", "PRODUCTOR", dir));

        // Residuo en almacen (entrada hace 10 dias, sin salida)
        Residuo rOk = new Residuo(100.0, "kg", "EN_ALMACEN", c);
        rOk.setCodigoLER("170405");
        rOk.setFechaEntradaAlmacen(LocalDateTime.now().minusDays(10));
        rOk.setDiasMaximoAlmacenamiento(180);
        residuoRepo.save(rOk);

        // Residuo critico (entrada hace 200 dias, max 180)
        Residuo rCritico = new Residuo(50.0, "litros", "EN_ALMACEN", c);
        rCritico.setCodigoLER("060101");
        rCritico.setFechaEntradaAlmacen(LocalDateTime.now().minusDays(200));
        rCritico.setDiasMaximoAlmacenamiento(180);
        residuoRepo.save(rCritico);
    }

    @Test
    @DisplayName("GET /api/almacen devuelve lista de residuos en almacen")
    void listarAlmacen_devuelveLista() throws Exception {
        mockMvc.perform(get("/api/almacen")
                .with(user(gestor.getEmail()).roles("GESTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/almacen/alertas-fifo devuelve solo residuos criticos")
    void alertasFifo_devuelveSoloCriticos() throws Exception {
        mockMvc.perform(get("/api/almacen/alertas-fifo")
                .with(user(gestor.getEmail()).roles("GESTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].severidad").value("CRITICO"));
    }

    @Test
    @DisplayName("GET /api/almacen sin autenticacion devuelve 401/403")
    void almacen_sinAuth_denegado() throws Exception {
        mockMvc.perform(get("/api/almacen"))
                .andExpect(status().is3xxRedirection());
    }
}
