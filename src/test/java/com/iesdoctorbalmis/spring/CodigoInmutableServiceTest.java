package com.iesdoctorbalmis.spring;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Direccion;
import com.iesdoctorbalmis.spring.modelo.Documento;
import com.iesdoctorbalmis.spring.modelo.Recogida;
import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.DireccionRepository;
import com.iesdoctorbalmis.spring.repository.DocumentoRepository;
import com.iesdoctorbalmis.spring.repository.EventoTrasladoRepository;
import com.iesdoctorbalmis.spring.repository.RecogidaRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.RutaRepository;
import com.iesdoctorbalmis.spring.repository.RutaTransportistaRepository;
import com.iesdoctorbalmis.spring.repository.TrasladoRepository;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;
import com.iesdoctorbalmis.spring.servicios.CentroServiceDB;
import com.iesdoctorbalmis.spring.servicios.DireccionServiceDB;
import com.iesdoctorbalmis.spring.servicios.DocumentoServiceDB;
import com.iesdoctorbalmis.spring.servicios.RecogidaService;
import com.iesdoctorbalmis.spring.servicios.ResiduoServiceDB;
import com.iesdoctorbalmis.spring.servicios.TrasladoServiceDB;
import com.iesdoctorbalmis.spring.servicios.UsuarioServiceDB;

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
        ResiduoServiceDB service = new ResiduoServiceDB(repo, centroRepo);

        Residuo existente = new Residuo();
        existente.setId(3L);
        existente.setCodigo("RES-001");
        when(repo.findById(3L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Residuo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Residuo editado = new Residuo();
        editado.setId(3L);

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
        TrasladoServiceDB service = new TrasladoServiceDB(
            trasladoRepo, eventoRepo, centroRepo, residuoRepo, usuarioRepo, documentoRepo, rutaRepo, rtRepo);

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