package com.iesdoctorbalmis.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.iesdoctorbalmis.spring.excepciones.TransicionEstadoInvalidaException;
import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Direccion;
import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.DireccionRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.TrasladoRepository;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;
import com.iesdoctorbalmis.spring.servicios.TrasladoService;

@SpringBootTest
@Transactional
class TrasladoServiceTest {

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
            new Usuario("Productor", "productor@test.com", "pass", Rol.PRODUCTOR));
        transportista = usuarioRepo.save(
            new Usuario("Transportista", "trans@test.com", "pass", Rol.TRANSPORTISTA));

        Direccion dir1 = direccionRepo.save(new Direccion("Calle 1", "Alicante", "03001", "Alicante", "Espana"));
        Direccion dir2 = direccionRepo.save(new Direccion("Calle 2", "Valencia", "46001", "Valencia", "Espana"));

        Centro centroProductor = centroRepo.save(new Centro(productor, "Centro Prod", "PRODUCTOR", dir1));
        Centro centroGestor = centroRepo.save(new Centro("Centro Gest", "GESTOR", dir2));

        Residuo residuo = residuoRepo.save(new Residuo(100.0, "kg", "PENDIENTE", centroProductor));

        traslado = trasladoRepo.save(new Traslado(centroProductor, centroGestor, residuo, transportista));
    }

    @Test
    @DisplayName("Transicion valida PENDIENTE -> EN_TRANSITO actualiza estado y fecha")
    void cambiarEstado_transicionValida() {
        Traslado resultado = trasladoService.cambiarEstado(
            traslado.getId(), EstadoTraslado.EN_TRANSITO, "Inicio transporte", transportista);

        assertThat(resultado.getEstado()).isEqualTo(EstadoTraslado.EN_TRANSITO);
        assertThat(resultado.getFechaInicioTransporte()).isNotNull();
    }

    @Test
    @DisplayName("Ciclo completo PENDIENTE -> EN_TRANSITO -> ENTREGADO -> COMPLETADO")
    void cambiarEstado_cicloCompleto() {
        trasladoService.cambiarEstado(traslado.getId(), EstadoTraslado.EN_TRANSITO, null, transportista);
        trasladoService.cambiarEstado(traslado.getId(), EstadoTraslado.ENTREGADO, null, transportista);
        Traslado final_ = trasladoService.cambiarEstado(traslado.getId(), EstadoTraslado.COMPLETADO, "OK", transportista);

        assertThat(final_.getEstado()).isEqualTo(EstadoTraslado.COMPLETADO);
        assertThat(final_.getFechaEntrega()).isNotNull();
        assertThat(final_.getFechaInicioTransporte()).isNotNull();
    }

    @Test
    @DisplayName("Transicion invalida PENDIENTE -> COMPLETADO lanza excepcion")
    void cambiarEstado_transicionInvalida() {
        assertThatThrownBy(() ->
            trasladoService.cambiarEstado(traslado.getId(), EstadoTraslado.COMPLETADO, null, transportista)
        ).isInstanceOf(TransicionEstadoInvalidaException.class);
    }

    @Test
    @DisplayName("Transicion invalida EN_TRANSITO -> PENDIENTE (retroceso) lanza excepcion")
    void cambiarEstado_retroceso() {
        trasladoService.cambiarEstado(traslado.getId(), EstadoTraslado.EN_TRANSITO, null, transportista);

        assertThatThrownBy(() ->
            trasladoService.cambiarEstado(traslado.getId(), EstadoTraslado.PENDIENTE, null, transportista)
        ).isInstanceOf(TransicionEstadoInvalidaException.class);
    }

    @Test
    @DisplayName("Historial registra todos los eventos de cambio de estado")
    void historial_registraEventos() {
        trasladoService.cambiarEstado(traslado.getId(), EstadoTraslado.EN_TRANSITO, "Salida", transportista);
        trasladoService.cambiarEstado(traslado.getId(), EstadoTraslado.ENTREGADO, "Llegada", transportista);

        var historial = trasladoService.historialDeTraslado(traslado.getId());
        assertThat(historial).hasSize(2);
        assertThat(historial.get(0).getEstadoNuevo()).isEqualTo(EstadoTraslado.ENTREGADO);
        assertThat(historial.get(1).getEstadoNuevo()).isEqualTo(EstadoTraslado.EN_TRANSITO);
    }
}
