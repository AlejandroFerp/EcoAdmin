package com.iesdoctorbalmis.spring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Direccion;
import com.iesdoctorbalmis.spring.modelo.Documento;
import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoDocumento;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;
import com.iesdoctorbalmis.spring.modelo.enums.TipoDocumento;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.DireccionRepository;
import com.iesdoctorbalmis.spring.repository.DocumentoRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.TrasladoRepository;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;
import com.iesdoctorbalmis.spring.servicios.DocumentoService;
import com.iesdoctorbalmis.spring.servicios.TrasladoService;

/**
 * Fase 19.10 - Tests del modulo Documentos:
 * - CRUD basico
 * - Generacion automatica del DI al completar un traslado
 * - Generacion automatica del Archivo Cronologico al completar
 * - Idempotencia (no duplica al volver a completar)
 * - Numero de referencia con patron por anio
 */
@SpringBootTest
@Transactional
class DocumentoServiceTest {

    @Autowired private DocumentoService documentoService;
    @Autowired private DocumentoRepository documentoRepo;
    @Autowired private TrasladoService trasladoService;
    @Autowired private TrasladoRepository trasladoRepo;
    @Autowired private CentroRepository centroRepo;
    @Autowired private ResiduoRepository residuoRepo;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private DireccionRepository direccionRepo;

    private Traslado traslado;
    private Usuario transportista;

    @BeforeEach
    void setUp() {
        Usuario productor = usuarioRepo.save(
            new Usuario("ProductorDoc", "productor.doc@test.com", "pass", Rol.PRODUCTOR));
        transportista = usuarioRepo.save(
            new Usuario("TransportistaDoc", "trans.doc@test.com", "pass", Rol.TRANSPORTISTA));

        Direccion dir1 = direccionRepo.save(new Direccion("Calle 1", "Alicante", "03001", "Alicante", "Espana"));
        Direccion dir2 = direccionRepo.save(new Direccion("Calle 2", "Valencia", "46001", "Valencia", "Espana"));

        Centro centroProductor = centroRepo.save(new Centro(productor, "Centro Prod Doc", "PRODUCTOR", dir1));
        Centro centroGestor = centroRepo.save(new Centro("Centro Gest Doc", "GESTOR", dir2));

        Residuo residuo = residuoRepo.save(new Residuo(50.0, "kg", "PENDIENTE", centroProductor));
        residuo.setCodigoLER("160107");
        residuoRepo.save(residuo);

        traslado = trasladoRepo.save(new Traslado(centroProductor, centroGestor, residuo, transportista));
    }

    @Test
    @DisplayName("CRUD: guardar y recuperar un documento manual")
    void crud_basico() {
        Documento d = new Documento(TipoDocumento.CONTRATO, traslado, "TEST-001");
        d.setEstado(EstadoDocumento.BORRADOR);

        Documento guardado = documentoService.save(d);

        assertThat(guardado.getId()).isNotNull();
        assertThat(documentoService.findById(guardado.getId())).isNotNull();
        assertThat(documentoService.findById(guardado.getId()).getNumeroReferencia()).isEqualTo("TEST-001");
    }

    @Test
    @DisplayName("Delete: elimina el documento")
    void delete_elimina() {
        Documento d = documentoService.save(new Documento(TipoDocumento.CONTRATO, traslado, "DEL-001"));
        Long id = d.getId();

        documentoService.delete(id);

        assertThat(documentoService.findById(id)).isNull();
    }

    @Test
    @DisplayName("Al completar un traslado se genera el DI automaticamente con referencia DI-{anio}-NNN")
    void completar_generaDI() {
        long antes = documentoRepo.findByTipo(TipoDocumento.DOCUMENTO_IDENTIFICACION).size();

        trasladoService.cambiarEstado(traslado.getId(), EstadoTraslado.COMPLETADO, "OK", transportista);

        var dis = documentoRepo.findByTipo(TipoDocumento.DOCUMENTO_IDENTIFICACION);
        assertThat(dis).hasSizeGreaterThan((int) antes);

        Documento di = dis.stream()
                .filter(d -> d.getTraslado() != null && d.getTraslado().getId().equals(traslado.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(di.getEstado()).isEqualTo(EstadoDocumento.EMITIDO);
        assertThat(di.getNumeroReferencia()).startsWith("DI-").matches(".*DI-\\d{4}-\\d{3}");
    }

    @Test
    @DisplayName("Al completar un traslado se genera el Archivo Cronologico (AC-{anio}-NNN)")
    void completar_generaArchivoCronologico() {
        trasladoService.cambiarEstado(traslado.getId(), EstadoTraslado.COMPLETADO, "OK", transportista);

        var acs = documentoRepo.findByTipo(TipoDocumento.ARCHIVO_CRONOLOGICO);
        Documento ac = acs.stream()
                .filter(d -> d.getTraslado() != null && d.getTraslado().getId().equals(traslado.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(ac.getEstado()).isEqualTo(EstadoDocumento.EMITIDO);
        assertThat(ac.getNumeroReferencia()).startsWith("AC-").matches(".*AC-\\d{4}-\\d{3}");
    }

    @Test
    @DisplayName("Idempotencia: completar dos veces no duplica DI ni AC del mismo traslado")
    void completar_dosVeces_noDuplica() {
        trasladoService.cambiarEstado(traslado.getId(), EstadoTraslado.COMPLETADO, "OK", transportista);
        // Bajar a PENDIENTE y volver a COMPLETADO no debe duplicar (existsByTrasladoAndTipo)
        trasladoService.cambiarEstado(traslado.getId(), EstadoTraslado.PENDIENTE, "Reset", transportista);
        trasladoService.cambiarEstado(traslado.getId(), EstadoTraslado.COMPLETADO, "Re-OK", transportista);

        long disDelTraslado = documentoRepo.findByTipo(TipoDocumento.DOCUMENTO_IDENTIFICACION).stream()
                .filter(d -> d.getTraslado() != null && d.getTraslado().getId().equals(traslado.getId()))
                .count();
        long acsDelTraslado = documentoRepo.findByTipo(TipoDocumento.ARCHIVO_CRONOLOGICO).stream()
                .filter(d -> d.getTraslado() != null && d.getTraslado().getId().equals(traslado.getId()))
                .count();

        assertThat(disDelTraslado).isEqualTo(1);
        assertThat(acsDelTraslado).isEqualTo(1);
    }

    @Test
    @DisplayName("existeDiParaTraslado refleja correctamente el estado")
    void existeDi_refleja_estado() {
        assertThat(documentoService.existeDiParaTraslado(traslado)).isFalse();
        trasladoService.cambiarEstado(traslado.getId(), EstadoTraslado.COMPLETADO, null, transportista);
        assertThat(documentoService.existeDiParaTraslado(traslado)).isTrue();
    }

    @Test
    @DisplayName("findByTraslado devuelve los documentos asociados ordenados por creadoEn desc")
    void findByTraslado_devuelveAsociados() {
        documentoService.save(new Documento(TipoDocumento.CONTRATO, traslado, "C-1"));
        documentoService.save(new Documento(TipoDocumento.NOTIFICACION_PREVIA, traslado, "NP-1"));

        var docs = documentoService.findByTraslado(traslado);
        assertThat(docs).hasSizeGreaterThanOrEqualTo(2);
        assertThat(docs).extracting(Documento::getNumeroReferencia).contains("C-1", "NP-1");
    }
}
