package com.iesdoctorbalmis.spring;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iesdoctorbalmis.spring.dto.DocumentoDraftCreateDTO;
import com.iesdoctorbalmis.spring.dto.DocumentoWorkflowDTO;
import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Documento;
import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoDocumento;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;
import com.iesdoctorbalmis.spring.modelo.enums.TipoDocumento;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.DocumentoRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.TrasladoRepository;

/**
 * Tests End-to-End para el módulo de Documentos (Fase 19).
 * 
 * Flujo validado:
 * 1. Crear documento borrador (draft) con metadatos específicos por tipo.
 * 2. Generar PDF automático para tipos generables (DI, NP).
 * 3. Adjuntar PDF externo para tipos que lo requieren (CONTRATO).
 * 4. Descargar y validar PDF descargado.
 * 5. Listar documentos y validar estados.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("E2E: Módulo de Documentos - Flujo Completo")
public class DocumentoE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DocumentoRepository docRepo;

    @Autowired
    private TrasladoRepository trasladoRepo;

    @Autowired
    private CentroRepository centroRepo;

    @Autowired
    private ResiduoRepository residuoRepo;

    private Centro centroProductor;
    private Centro centroGestor;
    private Residuo residuo;
    private Traslado traslado;

    @BeforeEach
    void setup() {
        // Crear centros
        centroProductor = new Centro();
        centroProductor.setNombre("Centro Productor Test");
        centroProductor = centroRepo.save(centroProductor);

        centroGestor = new Centro();
        centroGestor.setNombre("Centro Gestor Test");
        centroGestor = centroRepo.save(centroGestor);

        // Crear residuo
        residuo = new Residuo();
        residuo.setCodigoLER("16 06 01*");
        residuo.setCantidad(100.5);
        residuo.setUnidad("kg");
        residuo.setEstado("ACTIVO");
        residuo = residuoRepo.save(residuo);

        // Crear traslado
        traslado = new Traslado();
        traslado.setCentroProductor(centroProductor);
        traslado.setCentroGestor(centroGestor);
        traslado.setResiduo(residuo);
        traslado.setEstado(EstadoTraslado.PENDIENTE);
        traslado = trasladoRepo.save(traslado);
    }

    @Test
    @DisplayName("E2E 1: Crear DI (Documento Identificación) en borrador y generar PDF")
    void testCrearYGenerarDI() throws Exception {
        // 1. Crear draft de DI
        DocumentoDraftCreateDTO draft = new DocumentoDraftCreateDTO(
                TipoDocumento.DOCUMENTO_IDENTIFICACION,
                traslado.getId(),
                null,
                "DI-2025-001",
                null,
                null,
                "Test DI generado automáticamente",
                java.util.Map.of(
                    "ler", "16 06 01*",
                    "cantidad", 100.5
                )
        );

        MvcResult crearResult = mockMvc.perform(post("/api/documentos/drafts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(draft)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.siguienteAccion").value("GENERAR_PDF"))
                .andReturn();

        DocumentoWorkflowDTO workflow = objectMapper.readValue(
                crearResult.getResponse().getContentAsString(), DocumentoWorkflowDTO.class);
        Long docId = workflow.id();

        // 2. Validar que documento fue creado en BD
        Documento docEnBD = docRepo.findById(docId).orElseThrow();
        assertThat(docEnBD.getTipo()).isEqualTo(TipoDocumento.DOCUMENTO_IDENTIFICACION);
        assertThat(docEnBD.getEstado()).isEqualTo(EstadoDocumento.BORRADOR);
        assertThat(docEnBD.getTraslado().getId()).isEqualTo(traslado.getId());

        // 3. Generar PDF
        MvcResult generarResult = mockMvc.perform(post("/api/documentos/{id}/generar", docId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workflow.id").value(docId))
                .andExpect(jsonPath("$.workflow.estado").value("EMITIDO"))
                .andExpect(jsonPath("$.workflow.siguienteAccion").value("LISTO"))
                .andExpect(jsonPath("$.pdfUrl").isString())
                .andReturn();

        // 4. Validar estado post-generación
        docEnBD = docRepo.findById(docId).orElseThrow();
        assertThat(docEnBD.getEstado()).isEqualTo(EstadoDocumento.EMITIDO);
        assertThat(docEnBD.getFechaEmision()).isNotNull();
    }

    @Test
    @DisplayName("E2E 2: Crear Notificación Previa con metadatos y generar")
    void testCrearYGenerarNP() throws Exception {
        DocumentoDraftCreateDTO draft = new DocumentoDraftCreateDTO(
                TipoDocumento.NOTIFICACION_PREVIA,
                traslado.getId(),
                null,
                "NP-2025-001",
                null,
                null,
                "Test NP",
                java.util.Map.of(
                    "fechaPrevista", "2025-05-30",
                    "diasAntelacion", 15
                )
        );

        MvcResult crearResult = mockMvc.perform(post("/api/documentos/drafts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(draft)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.tipo").value("NOTIFICACION_PREVIA"))
                .andReturn();

        DocumentoWorkflowDTO workflow = objectMapper.readValue(
                crearResult.getResponse().getContentAsString(), DocumentoWorkflowDTO.class);
        Long docId = workflow.id();

        // Generar
        mockMvc.perform(post("/api/documentos/{id}/generar", docId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workflow.estado").value("EMITIDO"));
    }

    @Test
    @DisplayName("E2E 3: Crear CONTRATO (requiere adjunto manual)")
    void testCrearContratoConAdjunto() throws Exception {
        DocumentoDraftCreateDTO draft = new DocumentoDraftCreateDTO(
                TipoDocumento.CONTRATO,
                null,
                centroProductor.getId(),
                "CONTRATO-2025-001",
                null,
                null,
                "Contrato test",
                java.util.Map.of(
                    "contraparte", "Empresa Contratista S.L.",
                    "fechaFirma", "2025-01-15"
                )
        );

        MvcResult crearResult = mockMvc.perform(post("/api/documentos/drafts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(draft)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_ADJUNTO"))
                .andExpect(jsonPath("$.requiereAdjunto").value(true))
                .andExpect(jsonPath("$.siguienteAccion").value("SUBIR_PDF"))
                .andReturn();

        DocumentoWorkflowDTO workflow = objectMapper.readValue(
                crearResult.getResponse().getContentAsString(), DocumentoWorkflowDTO.class);

        // Validar que NO puede generarse automáticamente
        mockMvc.perform(post("/api/documentos/{id}/generar", workflow.id()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("E2E 4: Validar que DOCUMENTO_IDENTIFICACION sin trasladoId falla")
    void testDIsinTrasladoFalla() throws Exception {
        DocumentoDraftCreateDTO draft = new DocumentoDraftCreateDTO(
                TipoDocumento.DOCUMENTO_IDENTIFICACION,
                null, // ← traslado_id requerido
                null,
                "DI-FAIL",
                null,
                null,
                null,
                java.util.Map.of()
        );

        mockMvc.perform(post("/api/documentos/drafts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(draft)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("E2E 5: Validar que ARCHIVO_CRONOLOGICO no se crea manualmente")
    void testArchivoCronologicoNoSeCreaManu() throws Exception {
        DocumentoDraftCreateDTO draft = new DocumentoDraftCreateDTO(
                TipoDocumento.ARCHIVO_CRONOLOGICO,
                traslado.getId(),
                null,
                "AC-FAIL",
                null,
                null,
                null,
                java.util.Map.of()
        );

        mockMvc.perform(post("/api/documentos/drafts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(draft)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("E2E 6: Listar documentos y validar estados visuales")
    void testListarDocumentos() throws Exception {
        // Crear 2 documentos en diferentes estados
        Documento di = new Documento();
        di.setTipo(TipoDocumento.DOCUMENTO_IDENTIFICACION);
        di.setEstado(EstadoDocumento.EMITIDO);
        di.setTraslado(traslado);
        di = docRepo.save(di);

        Documento contrato = new Documento();
        contrato.setTipo(TipoDocumento.CONTRATO);
        contrato.setEstado(EstadoDocumento.PENDIENTE_ADJUNTO);
        contrato.setCentro(centroProductor);
        contrato = docRepo.save(contrato);

        mockMvc.perform(get("/api/documentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.estado == 'EMITIDO')]").exists())
                .andExpect(jsonPath("$[?(@.estado == 'PENDIENTE_ADJUNTO')]").exists());
    }

    @Test
    @DisplayName("E2E 7: Workflow endpoint devuelve siguiente acción correcta")
    void testWorkflowEndpoint() throws Exception {
        // Crear DI borrador
        Documento doc = new Documento();
        doc.setTipo(TipoDocumento.DOCUMENTO_IDENTIFICACION);
        doc.setEstado(EstadoDocumento.BORRADOR);
        doc.setTraslado(traslado);
        doc = docRepo.save(doc);

        mockMvc.perform(get("/api/documentos/{id}/workflow", doc.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.siguienteAccion").value("GENERAR_PDF"))
                .andExpect(jsonPath("$.requiereAdjunto").value(false));
    }

    @Test
    @DisplayName("E2E 8: Metadatos se validan por tipo (cantidad debe ser numérica)")
    void testValidacionMetadatosEstrict() throws Exception {
        DocumentoDraftCreateDTO draft = new DocumentoDraftCreateDTO(
                TipoDocumento.DOCUMENTO_IDENTIFICACION,
                traslado.getId(),
                null,
                "DI-META-TEST",
                null,
                null,
                null,
                java.util.Map.of(
                    "ler", "16 06 01*",
                    "cantidad", "NO-ES-NUMERO" // ← Debe ser número
                )
        );

        mockMvc.perform(post("/api/documentos/drafts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(draft)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("E2E 9: Flujo completo: crear → generar → descargar")
    void testFlujoCompletoE2E() throws Exception {
        // 1. Crear
        DocumentoDraftCreateDTO draft = new DocumentoDraftCreateDTO(
                TipoDocumento.NOTIFICACION_PREVIA,
                traslado.getId(),
                null,
                "NP-E2E-FULL",
                null,
                null,
                "Flujo completo NP",
                java.util.Map.of("fechaPrevista", "2025-06-01", "diasAntelacion", 14)
        );

        MvcResult crearResult = mockMvc.perform(post("/api/documentos/drafts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(draft)))
                .andExpect(status().isCreated())
                .andReturn();

        DocumentoWorkflowDTO workflow = objectMapper.readValue(
                crearResult.getResponse().getContentAsString(), DocumentoWorkflowDTO.class);
        Long docId = workflow.id();

        // 2. Generar
        mockMvc.perform(post("/api/documentos/{id}/generar", docId))
                .andExpect(status().isOk());

        // 3. Descargar
        mockMvc.perform(get("/api/documentos/{id}/archivo", docId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));

        // 4. Verificar estado final en BD
        Documento docFinal = docRepo.findById(docId).orElseThrow();
        assertThat(docFinal.getEstado()).isEqualTo(EstadoDocumento.EMITIDO);
    }
}
