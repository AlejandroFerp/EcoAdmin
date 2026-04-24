package com.iesdoctorbalmis.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.iesdoctorbalmis.spring.dto.RutaTransportistaInputDTO;
import com.iesdoctorbalmis.spring.dto.RutaTransportistaViewDTO;
import com.iesdoctorbalmis.spring.excepciones.RecursoNoEncontradoException;
import com.iesdoctorbalmis.spring.modelo.Ruta;
import com.iesdoctorbalmis.spring.modelo.RutaTransportista;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoRuta;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;
import com.iesdoctorbalmis.spring.repository.RutaRepository;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;
import com.iesdoctorbalmis.spring.servicios.RutaService;
import com.iesdoctorbalmis.spring.servicios.RutaTransportistaService;

import java.util.List;
import java.util.Map;

@SpringBootTest
@Transactional
class RutaTransportistaTest {

    @Autowired private RutaTransportistaService rtService;
    @Autowired private RutaService rutaService;
    @Autowired private RutaRepository rutaRepo;
    @Autowired private UsuarioRepository usuarioRepo;

    private Ruta rutaConDistancia;
    private Usuario transportista1;
    private Usuario transportista2;

    @BeforeEach
    void setUp() {
        transportista1 = usuarioRepo.save(
            new Usuario("Trans Uno", "trans1@test.com", "pass", Rol.TRANSPORTISTA));
        transportista2 = usuarioRepo.save(
            new Usuario("Trans Dos", "trans2@test.com", "pass", Rol.TRANSPORTISTA));

        Ruta datos = new Ruta();
        datos.setNombre("Ruta Test Tarifas");
        datos.setEstado(EstadoRuta.PLANIFICADA);
        datos.setDistanciaKm(100.0);
        datos.setFormulaTarifa("w * 0.3 + L * 0.05"); // fórmula base de la ruta
        datos.setUnidadTarifa("EUR");
        rutaConDistancia = rutaService.crear(datos, null);
    }

    // ———————————————————————————————————————————
    // asignar()
    // ———————————————————————————————————————————

    @Test
    @DisplayName("asignar vincula transportista a la ruta correctamente")
    void asignar_creaVinculo() {
        var dto = new RutaTransportistaInputDTO(transportista1.getId(), "w * 0.4 + L * 0.08", "EUR");
        RutaTransportista rt = rtService.asignar(rutaConDistancia.getId(), dto);

        assertThat(rt.getId()).isNotNull();
        assertThat(rt.getTransportista().getId()).isEqualTo(transportista1.getId());
        assertThat(rt.getFormulaTarifa()).isEqualTo("w * 0.4 + L * 0.08");
        assertThat(rt.isActivo()).isTrue();
    }

    @Test
    @DisplayName("asignar mismo transportista dos veces reactiva y actualiza la asignación")
    void asignar_duplicado_reactivaYActualiza() {
        var dto1 = new RutaTransportistaInputDTO(transportista1.getId(), "w * 0.4", "EUR");
        rtService.asignar(rutaConDistancia.getId(), dto1);

        var dto2 = new RutaTransportistaInputDTO(transportista1.getId(), "w * 0.5", "USD");
        RutaTransportista rt = rtService.asignar(rutaConDistancia.getId(), dto2);

        assertThat(rt.getFormulaTarifa()).isEqualTo("w * 0.5");
        assertThat(rt.getUnidadTarifa()).isEqualTo("USD");
        assertThat(rt.isActivo()).isTrue();

        // Solo debe existir un vínculo para este transportista
        List<RutaTransportistaViewDTO> lista = rtService.listarConPrecio(rutaConDistancia.getId());
        long count = lista.stream()
            .filter(v -> v.transportistaId().equals(transportista1.getId()))
            .count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("asignar con fórmula inválida lanza IllegalArgumentException")
    void asignar_formulaInvalida_lanzaExcepcion() {
        var dto = new RutaTransportistaInputDTO(transportista1.getId(), "w +++ L", null);

        assertThatThrownBy(() -> rtService.asignar(rutaConDistancia.getId(), dto))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("asignar a ruta inexistente lanza RecursoNoEncontradoException")
    void asignar_rutaInexistente_lanzaExcepcion() {
        var dto = new RutaTransportistaInputDTO(transportista1.getId(), null, null);

        assertThatThrownBy(() -> rtService.asignar(99999L, dto))
            .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ———————————————————————————————————————————
    // listarConPrecio() — fórmula propia vs fallback
    // ———————————————————————————————————————————

    @Test
    @DisplayName("listarConPrecio usa fórmula propia cuando el transportista la tiene")
    void listar_usaFormulaPropia() {
        // Trans1 tiene fórmula propia
        rtService.asignar(rutaConDistancia.getId(),
            new RutaTransportistaInputDTO(transportista1.getId(), "w * 0.5 + L * 0.1", "EUR"));

        List<RutaTransportistaViewDTO> lista = rtService.listarConPrecio(rutaConDistancia.getId());

        assertThat(lista).hasSize(1);
        RutaTransportistaViewDTO view = lista.get(0);
        assertThat(view.formulaEfectiva()).isEqualTo("w * 0.5 + L * 0.1");
        // Precio: w=100, L=100 → 100*0.5 + 100*0.1 = 60.0
        assertThat(view.precioEjemplo()).isEqualTo(60.0);
    }

    @Test
    @DisplayName("listarConPrecio usa fórmula de la ruta como fallback cuando el transportista no tiene formula propia")
    void listar_usaFallbackRuta() {
        // Trans2 NO tiene fórmula propia → debe usar la de la ruta
        rtService.asignar(rutaConDistancia.getId(),
            new RutaTransportistaInputDTO(transportista2.getId(), null, null));

        List<RutaTransportistaViewDTO> lista = rtService.listarConPrecio(rutaConDistancia.getId());

        assertThat(lista).hasSize(1);
        RutaTransportistaViewDTO view = lista.get(0);
        assertThat(view.formulaTarifa()).isNull();                         // no tiene propia
        assertThat(view.formulaEfectiva()).isEqualTo("w * 0.3 + L * 0.05"); // fallback ruta
        // Precio: w=100, L=100 → 100*0.3 + 100*0.05 = 35.0
        assertThat(view.precioEjemplo()).isEqualTo(35.0);
    }

    @Test
    @DisplayName("listarConPrecio devuelve precioEjemplo null cuando no hay fórmula en ningún nivel")
    void listar_sinFormula_precioNull() {
        // Ruta sin fórmula base
        Ruta rutaSinFormula = new Ruta();
        rutaSinFormula.setNombre("Ruta sin tarifa");
        rutaSinFormula.setEstado(EstadoRuta.PLANIFICADA);
        rutaSinFormula.setDistanciaKm(50.0);
        Ruta guardada = rutaService.crear(rutaSinFormula, null);

        rtService.asignar(guardada.getId(),
            new RutaTransportistaInputDTO(transportista1.getId(), null, null));

        List<RutaTransportistaViewDTO> lista = rtService.listarConPrecio(guardada.getId());

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).formulaEfectiva()).isNull();
        assertThat(lista.get(0).precioEjemplo()).isNull();
    }

    // ———————————————————————————————————————————
    // desasignar()
    // ———————————————————————————————————————————

    @Test
    @DisplayName("desasignar elimina la asignación de la ruta")
    void desasignar_eliminaVinculo() {
        rtService.asignar(rutaConDistancia.getId(),
            new RutaTransportistaInputDTO(transportista1.getId(), "w * 0.3", "EUR"));

        rtService.desasignar(rutaConDistancia.getId(), transportista1.getId());

        assertThat(rtService.listarConPrecio(rutaConDistancia.getId())).isEmpty();
    }

    // ———————————————————————————————————————————
    // perteneceARuta()
    // ———————————————————————————————————————————

    @Test
    @DisplayName("perteneceARuta devuelve true para transportistas asignados y false para no asignados")
    void perteneceARuta_distingueAsignadoYNoAsignado() {
        rtService.asignar(rutaConDistancia.getId(),
            new RutaTransportistaInputDTO(transportista1.getId(), null, null));

        assertThat(rtService.perteneceARuta(rutaConDistancia.getId(), transportista1.getId())).isTrue();
        assertThat(rtService.perteneceARuta(rutaConDistancia.getId(), transportista2.getId())).isFalse();
    }

    // ———————————————————————————————————————————
    // calcularPrecio()
    // ———————————————————————————————————————————

    @Test
    @DisplayName("calcularPrecio con fórmula propia devuelve resultado correcto")
    void calcular_conFormulaPropia() {
        rtService.asignar(rutaConDistancia.getId(),
            new RutaTransportistaInputDTO(transportista1.getId(), "w * 2.0", "EUR"));

        Map<String, Object> res = rtService.calcularPrecio(
            rutaConDistancia.getId(), transportista1.getId(), 50.0);

        assertThat(res).containsKey("resultado");
        assertThat((Double) res.get("resultado")).isEqualTo(100.0); // 50 * 2 = 100
        assertThat(res.get("formulaPropia")).isEqualTo(true);
    }

    @Test
    @DisplayName("calcularPrecio sin fórmula devuelve error")
    void calcular_sinFormula_devuelveError() {
        Ruta rutaVacia = new Ruta();
        rutaVacia.setNombre("Sin tarifa");
        rutaVacia.setEstado(EstadoRuta.PLANIFICADA);
        Ruta guardada = rutaService.crear(rutaVacia, null);

        rtService.asignar(guardada.getId(),
            new RutaTransportistaInputDTO(transportista1.getId(), null, null));

        Map<String, Object> res = rtService.calcularPrecio(
            guardada.getId(), transportista1.getId(), 100.0);

        assertThat(res).containsKey("error");
    }
}
