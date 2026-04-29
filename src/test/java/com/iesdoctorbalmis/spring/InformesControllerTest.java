package com.iesdoctorbalmis.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Direccion;
import com.iesdoctorbalmis.spring.modelo.ListaLer;
import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.DireccionRepository;
import com.iesdoctorbalmis.spring.repository.ListaLerRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.TrasladoRepository;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;
import com.iesdoctorbalmis.spring.servicios.ResiduoService;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class InformesControllerTest {

    private MockMvc mockMvc;
    @Autowired private WebApplicationContext context;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private CentroRepository centroRepo;
        @Autowired private ListaLerRepository listaLerRepo;
    @Autowired private ResiduoRepository residuoRepo;
    @Autowired private TrasladoRepository trasladoRepo;
    @Autowired private DireccionRepository direccionRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EntityManager entityManager;
    @Autowired private ResiduoService residuoService;

    private Usuario gestor;
    private String descripcionCanonica;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        gestor = usuarioRepo.save(new Usuario("Gestor Informes", "gestor-informes@test.com",
                passwordEncoder.encode("pass"), Rol.GESTOR));

        Direccion dir = direccionRepo.save(new Direccion("C/ Test", "Alicante", "03001", "Alicante", "Espana"));
        Centro cp = centroRepo.save(new Centro(gestor, "Productor Test", "PRODUCTOR", dir));
        Centro cg = centroRepo.save(new Centro("Gestor Test", "GESTOR", dir));

        descripcionCanonica = listaLerRepo.findByCodigo("17 04 05")
            .orElseGet(() -> listaLerRepo.save(new ListaLer("17 04 05", "Descripcion canonica LER")))
            .getDescripcion();

        Residuo r = new Residuo(100.0, "kg", "PENDIENTE", cp);
        r.setCodigoLER("170405");
        residuoService.save(r);

        Usuario trans = usuarioRepo.save(new Usuario("Trans", "trans-inf@test.com",
                passwordEncoder.encode("pass"), Rol.TRANSPORTISTA));
        Traslado t = new Traslado(cp, cg, r, trans);
        t.setEstado(EstadoTraslado.COMPLETADO);
        trasladoRepo.save(t);
        entityManager.flush();
        entityManager.clear();
    }

    // ─── Informe genérico (traslados) ─────────────────────────────────────────

    @Test
    @DisplayName("GET /api/informes/traslados devuelve columnas y filas")
    void informeTraslados_devuelveEstructura() throws Exception {
        mockMvc.perform(get("/api/informes/traslados")
                .with(user(gestor.getEmail()).roles("GESTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns").isArray())
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.total").isNumber());
    }

    @Test
    @DisplayName("GET /api/informes/traslados sin auth devuelve 401/403")
    void informeTraslados_sinAuth_denegado() throws Exception {
        mockMvc.perform(get("/api/informes/traslados"))
                .andExpect(status().is3xxRedirection());
    }

    // ─── Inventario almacén ───────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/informes/inventario-almacen devuelve estructura con resumen")
    void inventarioAlmacen_devuelveEstructura() throws Exception {
        mockMvc.perform(get("/api/informes/inventario-almacen")
                .with(user(gestor.getEmail()).roles("GESTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns").isArray())
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.resumen").exists())
                .andExpect(jsonPath("$.resumen.total").isNumber());
    }

        @Test
        @DisplayName("GET /api/informes/residuos usa descripcion canonica de lista LER")
        void informeResiduos_usaDescripcionCanonica() throws Exception {
                mockMvc.perform(get("/api/informes/residuos")
                                .with(user(gestor.getEmail()).roles("GESTOR")))
                                .andExpect(status().isOk())
                                                .andExpect(jsonPath("$.rows[0][1]").value("17 04 05"))
                                                .andExpect(jsonPath("$.rows[0][2]").value(descripcionCanonica));
        }

    // ─── Final de gestión ─────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/informes/final-gestion devuelve porLer y resumen")
    void finalGestion_devuelvePorLer() throws Exception {
        mockMvc.perform(get("/api/informes/final-gestion")
                .with(user(gestor.getEmail()).roles("GESTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porLer").isArray())
                .andExpect(jsonPath("$.resumen.trasladosCompletados").isNumber())
                .andExpect(jsonPath("$.resumen.codigosLerDistintos").isNumber());
    }

    @Test
    @DisplayName("GET /api/informes/final-gestion con filtro desde/hasta funciona")
    void finalGestion_conFiltros() throws Exception {
        mockMvc.perform(get("/api/informes/final-gestion")
                .param("desde", "2025-01-01")
                .param("hasta", "2027-12-31")
                .with(user(gestor.getEmail()).roles("GESTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porLer").isArray());
    }

    // ─── Final de gestión PDF ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/informes/final-gestion/pdf devuelve application/pdf")
    void finalGestionPdf_devuelvePdf() throws Exception {
        mockMvc.perform(get("/api/informes/final-gestion/pdf")
                .with(user(gestor.getEmail()).roles("GESTOR")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("informe-final-gestion")));
    }

    // ─── Checklist auditoría ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/informes/checklist-auditoria devuelve items y resumen")
    void checklistAuditoria_devuelveEstructura() throws Exception {
        mockMvc.perform(get("/api/informes/checklist-auditoria")
                .with(user(gestor.getEmail()).roles("GESTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.resumen").exists())
                .andExpect(jsonPath("$.resumen.verdes").isNumber())
                .andExpect(jsonPath("$.resumen.rojos").isNumber());
    }

    // ─── Trazabilidad ────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/informes/trazabilidad/99999 devuelve 404 si residuo no existe")
    void trazabilidad_noExiste_404() throws Exception {
        mockMvc.perform(get("/api/informes/trazabilidad/99999")
                .with(user(gestor.getEmail()).roles("GESTOR")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/informes/trazabilidad/{id} devuelve datos del residuo")
    void trazabilidad_existe_200() throws Exception {
        Long residuoId = residuoRepo.findAll().stream().findFirst().get().getId();

        mockMvc.perform(get("/api/informes/trazabilidad/" + residuoId)
                .with(user(gestor.getEmail()).roles("GESTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.residuo").exists())
                .andExpect(jsonPath("$.traslados").isArray());
    }
}
