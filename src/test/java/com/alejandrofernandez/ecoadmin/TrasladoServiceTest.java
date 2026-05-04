package com.alejandrofernandez.ecoadmin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.alejandrofernandez.ecoadmin.excepciones.TransicionEstadoInvalidaException;
import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.Direccion;
import com.alejandrofernandez.ecoadmin.modelo.Residuo;
import com.alejandrofernandez.ecoadmin.modelo.Traslado;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoTraslado;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;
import com.alejandrofernandez.ecoadmin.repository.CentroRepository;
import com.alejandrofernandez.ecoadmin.repository.DireccionRepository;
import com.alejandrofernandez.ecoadmin.repository.ResiduoRepository;
import com.alejandrofernandez.ecoadmin.repository.TrasladoRepository;
import com.alejandrofernandez.ecoadmin.repository.UsuarioRepository;
import com.alejandrofernandez.ecoadmin.servicios.TrasladoService;

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
    @DisplayName("Cualquier estado es alcanzable desde cualquier otro (libertad de rectificacion)")
    void cambiarEstado_libertadTotal() {
        // De PENDIENTE a COMPLETADO (salto adelante)
        trasladoService.cambiarEstado(traslado.getId(), EstadoTraslado.COMPLETADO, "Salto", transportista);
        assertThat(trasladoRepo.findById(traslado.getId()).get().getEstado()).isEqualTo(EstadoTraslado.COMPLETADO);

        // De COMPLETADO a PENDIENTE (salto atras)
        trasladoService.cambiarEstado(traslado.getId(), EstadoTraslado.PENDIENTE, "Reset", transportista);
        assertThat(trasladoRepo.findById(traslado.getId()).get().getEstado()).isEqualTo(EstadoTraslado.PENDIENTE);
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
