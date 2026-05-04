package com.alejandrofernandez.ecoadmin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.Direccion;
import com.alejandrofernandez.ecoadmin.modelo.ListaLer;
import com.alejandrofernandez.ecoadmin.modelo.Residuo;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;
import com.alejandrofernandez.ecoadmin.repository.CentroRepository;
import com.alejandrofernandez.ecoadmin.repository.DireccionRepository;
import com.alejandrofernandez.ecoadmin.repository.ListaLerRepository;
import com.alejandrofernandez.ecoadmin.repository.ResiduoRepository;
import com.alejandrofernandez.ecoadmin.repository.UsuarioRepository;
import com.alejandrofernandez.ecoadmin.servicios.ResiduoService;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class ResiduoControllerTest {

    private MockMvc mockMvc;

    @Autowired private WebApplicationContext context;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private CentroRepository centroRepo;
    @Autowired private DireccionRepository direccionRepo;
    @Autowired private ListaLerRepository listaLerRepo;
    @Autowired private ResiduoRepository residuoRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EntityManager entityManager;
    @Autowired private ResiduoService residuoService;

    private Usuario gestor;
    private Residuo residuo;
    private String descripcionCanonica;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        gestor = usuarioRepo.save(new Usuario("Gestor Residuos", "gestor-residuos@test.com",
                passwordEncoder.encode("pass"), Rol.GESTOR));

        Direccion direccion = direccionRepo.save(new Direccion("C/ Residuos", "Alicante", "03001", "Alicante", "Espana"));
        Centro centro = centroRepo.save(new Centro(gestor, "Centro Residuos", "PRODUCTOR", direccion));

        descripcionCanonica = listaLerRepo.findByCodigo("17 04 05")
            .orElseGet(() -> listaLerRepo.save(new ListaLer("17 04 05", "Descripcion canonica LER")))
            .getDescripcion();

        Residuo creado = new Residuo(100.0, "kg", "PENDIENTE", centro);
        creado.setCodigoLER("170405");
        residuo = residuoService.save(creado);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("GET /api/residuos devuelve descripcion desde lista LER")
    void listarDevuelveDescripcionCanonica() throws Exception {
        mockMvc.perform(get("/api/residuos")
                .with(user(gestor.getEmail()).roles("GESTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + residuo.getId() + ")].codigoLER").value("17 04 05"))
                .andExpect(jsonPath("$[?(@.id==" + residuo.getId() + ")].descripcion").value(descripcionCanonica));
    }

    @Test
    @DisplayName("GET /api/residuos/{id} devuelve descripcion desde lista LER")
    void buscarDevuelveDescripcionCanonica() throws Exception {
        mockMvc.perform(get("/api/residuos/" + residuo.getId())
                .with(user(gestor.getEmail()).roles("GESTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoLER").value("17 04 05"))
                .andExpect(jsonPath("$.descripcion").value(descripcionCanonica));
    }
}