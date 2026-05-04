package com.alejandrofernandez.ecoadmin;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.Direccion;
import com.alejandrofernandez.ecoadmin.modelo.Documento;
import com.alejandrofernandez.ecoadmin.modelo.Recogida;
import com.alejandrofernandez.ecoadmin.modelo.Residuo;
import com.alejandrofernandez.ecoadmin.modelo.Traslado;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.repository.CentroRepository;
import com.alejandrofernandez.ecoadmin.repository.DireccionRepository;
import com.alejandrofernandez.ecoadmin.repository.DocumentoRepository;
import com.alejandrofernandez.ecoadmin.repository.EventoTrasladoRepository;
import com.alejandrofernandez.ecoadmin.repository.RecogidaRepository;
import com.alejandrofernandez.ecoadmin.repository.ResiduoRepository;
import com.alejandrofernandez.ecoadmin.repository.RutaRepository;
import com.alejandrofernandez.ecoadmin.repository.RutaTransportistaRepository;
import com.alejandrofernandez.ecoadmin.repository.TrasladoRepository;
import com.alejandrofernandez.ecoadmin.repository.UsuarioRepository;
import com.alejandrofernandez.ecoadmin.servicios.CentroServiceDB;
import com.alejandrofernandez.ecoadmin.servicios.DireccionServiceDB;
import com.alejandrofernandez.ecoadmin.servicios.DocumentoServiceDB;
import com.alejandrofernandez.ecoadmin.servicios.LerCodeResolver;
import com.alejandrofernandez.ecoadmin.servicios.RecogidaService;
import com.alejandrofernandez.ecoadmin.servicios.ResiduoServiceDB;
import com.alejandrofernandez.ecoadmin.servicios.TrasladoServiceDB;
import com.alejandrofernandez.ecoadmin.servicios.UsuarioServiceDB;

import jakarta.persistence.EntityManager;

class CodigoInmutableServiceTest {

    @Test
    @DisplayName("CentroServiceDB conserva el codigo existente al editar")
    void centroSave_conservaCodigoExistente() {
        CentroRepository repo = mock(CentroRepository.class);
        DireccionRepository direccionRepo = mock(DireccionRepository.class);
        CentroServiceDB service = new CentroServiceDB(repo, direccionRepo);

        Centro existente = new Centro();
        existente.setId(1L);
        existente.setCodigo("CEN-001");
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Centro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Centro editado = new Centro();
        editado.setId(1L);

        Centro guardado = service.save(editado);

        assertThat(guardado.getCodigo()).isEqualTo("CEN-001");
    }

    @Test
    @DisplayName("DireccionServiceDB conserva el codigo existente al editar")
    void direccionSave_conservaCodigoExistente() {
        DireccionRepository repo = mock(DireccionRepository.class);
        DireccionServiceDB service = new DireccionServiceDB(repo);

        Direccion existente = new Direccion();
        existente.setId(2L);
        existente.setCodigo("DIR-001");
        when(repo.findById(2L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Direccion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Direccion editada = new Direccion();
        editada.setId(2L);

        Direccion guardada = service.save(editada);

        assertThat(guardada.getCodigo()).isEqualTo("DIR-001");
    }

    @Test
    @DisplayName("ResiduoServiceDB conserva el codigo existente al editar")
    void residuoSave_conservaCodigoExistente() {
        ResiduoRepository repo = mock(ResiduoRepository.class);
        CentroRepository centroRepo = mock(CentroRepository.class);
        EntityManager entityManager = mock(EntityManager.class);
        LerCodeResolver lerCodeResolver = mock(LerCodeResolver.class);
        ResiduoServiceDB service = new ResiduoServiceDB(repo, centroRepo, entityManager, lerCodeResolver);

        Residuo existente = new Residuo();
        existente.setId(3L);
        existente.setCodigo("RES-001");
        when(repo.findById(3L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Residuo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(lerCodeResolver.requireCanonicalCode(anyString())).thenReturn("17 04 05");

        Residuo editado = new Residuo();
        editado.setId(3L);
        editado.setCodigoLER("170405");

        Residuo guardado = service.save(editado);

        assertThat(guardado.getCodigo()).isEqualTo("RES-001");
    }

    @Test
    @DisplayName("TrasladoServiceDB conserva el codigo existente al editar")
    void trasladoSave_conservaCodigoExistente() {
        TrasladoRepository trasladoRepo = mock(TrasladoRepository.class);
        EventoTrasladoRepository eventoRepo = mock(EventoTrasladoRepository.class);
        CentroRepository centroRepo = mock(CentroRepository.class);
        ResiduoRepository residuoRepo = mock(ResiduoRepository.class);
        UsuarioRepository usuarioRepo = mock(UsuarioRepository.class);
        DocumentoRepository documentoRepo = mock(DocumentoRepository.class);
        RutaRepository rutaRepo = mock(RutaRepository.class);
        RutaTransportistaRepository rtRepo = mock(RutaTransportistaRepository.class);
        EntityManager entityManager = mock(EntityManager.class);
        TrasladoServiceDB service = new TrasladoServiceDB(
            trasladoRepo, eventoRepo, centroRepo, residuoRepo, usuarioRepo, documentoRepo, rutaRepo, rtRepo, entityManager);

        Traslado existente = new Traslado();
        existente.setId(4L);
        existente.setCodigo("TRA-001");
        when(trasladoRepo.findById(4L)).thenReturn(Optional.of(existente));
        when(trasladoRepo.save(any(Traslado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Traslado editado = new Traslado();
        editado.setId(4L);

        Traslado guardado = service.save(editado);

        assertThat(guardado.getCodigo()).isEqualTo("TRA-001");
    }

    @Test
    @DisplayName("DocumentoServiceDB conserva el codigo existente al editar")
    void documentoSave_conservaCodigoExistente() {
        DocumentoRepository repo = mock(DocumentoRepository.class);
        TrasladoRepository trasladoRepo = mock(TrasladoRepository.class);
        DocumentoServiceDB service = new DocumentoServiceDB(repo, trasladoRepo);

        Documento existente = new Documento();
        existente.setId(5L);
        existente.setCodigo("DOC-001");
        when(repo.findById(5L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Documento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Documento editado = new Documento();
        editado.setId(5L);

        Documento guardado = service.save(editado);

        assertThat(guardado.getCodigo()).isEqualTo("DOC-001");
    }

    @Test
    @DisplayName("UsuarioServiceDB conserva el codigo existente al editar")
    void usuarioSave_conservaCodigoExistente() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UsuarioServiceDB service = new UsuarioServiceDB(repo, encoder);

        Usuario existente = new Usuario();
        existente.setId(6L);
        existente.setCodigo("USR-001");
        when(repo.findById(6L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario editado = new Usuario();
        editado.setId(6L);

        Usuario guardado = service.save(editado);

        assertThat(guardado.getCodigo()).isEqualTo("USR-001");
    }

    @Test
    @DisplayName("RecogidaService conserva el codigo existente al editar")
    void recogidaSave_conservaCodigoExistente() {
        RecogidaRepository repo = mock(RecogidaRepository.class);
        RecogidaService service = new RecogidaService(repo);

        Recogida existente = new Recogida();
        existente.setId(7L);
        existente.setCodigo("REC-001");
        when(repo.findById(7L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Recogida.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Recogida editada = new Recogida();
        editada.setId(7L);

        Recogida guardada = service.save(editada);

        assertThat(guardada.getCodigo()).isEqualTo("REC-001");
    }
}