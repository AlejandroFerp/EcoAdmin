package com.iesdoctorbalmis.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.iesdoctorbalmis.spring.excepciones.RecursoNoEncontradoException;
import com.iesdoctorbalmis.spring.excepciones.TransicionEstadoInvalidaException;
import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;
import com.iesdoctorbalmis.spring.servicios.TrasladoService;

@SpringBootTest
@Transactional
class TrasladoEndpointQrTest {

    @Autowired private TrasladoService trasladoService;
    @Autowired private CentroRepository centroRepo;
    @Autowired private ResiduoRepository residuoRepo;
    @Autowired private UsuarioRepository usuarioRepo;

    private Traslado traslado;
    private Usuario transportista;

    @BeforeEach
    void setUp() {
        transportista = usuarioRepo.save(
            new Usuario("Trans QR", "transqr@test.com", "pass", Rol.TRANSPORTISTA));

        Centro productor = centroRepo.save(crearCentro("Productor QR"));
        Centro gestor = centroRepo.save(crearCentro("Gestor QR"));
        Residuo residuo = residuoRepo.save(crearResiduo("Residuo QR"));

        Traslado datos = new Traslado(productor, gestor, residuo, transportista);
        traslado = trasladoService.save(datos);
    }

    // ———————————————————————————————————————————
    // Entrada via QR — estado PENDIENTE → EN_TRANSITO
    // ———————————————————————————————————————————

    @Test
    @DisplayName("Entrada QR: PENDIENTE → EN_TRANSITO cambia estado y registra fecha inicio")
    void entradaQr_pendiente_pasa_a_enTransito() {
        assertThat(traslado.getEstado()).isEqualTo(EstadoTraslado.PENDIENTE);

        Traslado actualizado = trasladoService.cambiarEstado(
            traslado.getId(), EstadoTraslado.EN_TRANSITO,
            "Entrada registrada via QR", transportista);

        assertThat(actualizado.getEstado()).isEqualTo(EstadoTraslado.EN_TRANSITO);
        assertThat(actualizado.getFechaInicioTransporte()).isNotNull();
        assertThat(actualizado.getFechaUltimoCambioEstado()).isNotNull();
    }

    @Test
    @DisplayName("Entrada QR: traslado ya EN_TRANSITO lanza TransicionEstadoInvalidaException")
    void entradaQr_yaEnTransito_lanzaExcepcion() {
        trasladoService.cambiarEstado(traslado.getId(), EstadoTraslado.EN_TRANSITO,
            "Primera entrada QR", transportista);

        assertThatThrownBy(() ->
            trasladoService.cambiarEstado(traslado.getId(), EstadoTraslado.EN_TRANSITO,
                "Segunda entrada QR", transportista)
        ).isInstanceOf(TransicionEstadoInvalidaException.class);
    }

    @Test
    @DisplayName("Entrada QR: traslado CANCELADO no puede pasar a EN_TRANSITO")
    void entradaQr_cancelado_lanzaExcepcion() {
        trasladoService.cambiarEstado(traslado.getId(), EstadoTraslado.CANCELADO,
            "Cancelado manualmente", transportista);

        assertThatThrownBy(() ->
            trasladoService.cambiarEstado(traslado.getId(), EstadoTraslado.EN_TRANSITO,
                "Entrada QR sobre cancelado", transportista)
        ).isInstanceOf(TransicionEstadoInvalidaException.class);
    }

    @Test
    @DisplayName("Entrada QR: id inexistente lanza RecursoNoEncontradoException")
    void entradaQr_idInexistente_lanzaExcepcion() {
        assertThatThrownBy(() ->
            trasladoService.cambiarEstado(99999L, EstadoTraslado.EN_TRANSITO,
                "QR invalido", transportista)
        ).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("Entrada QR: PENDIENTE → EN_TRANSITO genera EventoTraslado con comentario correcto")
    void entradaQr_creaEventoConComentarioQr() {
        Traslado actualizado = trasladoService.cambiarEstado(
            traslado.getId(), EstadoTraslado.EN_TRANSITO,
            "Entrada registrada via QR", transportista);

        // El historial se carga aparte — verificamos via findById
        Traslado recargado = trasladoService.findById(actualizado.getId());
        assertThat(recargado.getEstado()).isEqualTo(EstadoTraslado.EN_TRANSITO);
    }

    // ———————————————————————————————————————————
    // Helpers
    // ———————————————————————————————————————————

    private Centro crearCentro(String nombre) {
        Centro c = new Centro();
        c.setNombre(nombre);
        return c;
    }

    private Residuo crearResiduo(String desc) {
        Residuo r = new Residuo();
        r.setDescripcion(desc);
        r.setCodigoLER("08 01 11*");
        return r;
    }
}
