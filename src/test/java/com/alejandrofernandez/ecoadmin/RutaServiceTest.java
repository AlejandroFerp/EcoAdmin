package com.alejandrofernandez.ecoadmin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.alejandrofernandez.ecoadmin.dto.RutaInputDTO;
import com.alejandrofernandez.ecoadmin.modelo.Ruta;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoRuta;
import com.alejandrofernandez.ecoadmin.repository.RutaRepository;
import com.alejandrofernandez.ecoadmin.servicios.RutaService;

@SpringBootTest
@Transactional
class RutaServiceTest {

    @Autowired private RutaService rutaService;
    @Autowired private RutaRepository rutaRepo;

    private RutaInputDTO dto(String nombre, EstadoRuta estado) {
        return new RutaInputDTO(nombre, null, estado, null, null, null, null, null, null);
    }

    // ———————————————————————————————————————————
    // crear()
    // ———————————————————————————————————————————

    @Test
    @DisplayName("crear ruta con nombre válido la persiste correctamente")
    void crear_conNombreValido_persiste() {
        RutaInputDTO input = dto("Ruta Norte", EstadoRuta.PLANIFICADA);

        Ruta guardada = rutaService.crear(input);

        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getNombre()).isEqualTo("Ruta Norte");
        assertThat(guardada.getEstado()).isEqualTo(EstadoRuta.PLANIFICADA);
    }

    @Test
    @DisplayName("crear ruta sin nombre lanza IllegalArgumentException")
    void crear_sinNombre_lanzaExcepcion() {
        RutaInputDTO input = dto("  ", EstadoRuta.PLANIFICADA);

        assertThatThrownBy(() -> rutaService.crear(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");
    }

    @Test
    @DisplayName("crear ruta con fórmula inválida lanza IllegalArgumentException")
    void crear_conFormulaInvalida_lanzaExcepcion() {
        RutaInputDTO input = new RutaInputDTO("Ruta Tarifas", null, EstadoRuta.PLANIFICADA,
                null, null, null, null, "w +++ L", null);

        assertThatThrownBy(() -> rutaService.crear(input))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("crear ruta con fórmula válida la persiste")
    void crear_conFormulaValida_persiste() {
        RutaInputDTO input = new RutaInputDTO("Ruta con Tarifa", null, EstadoRuta.PLANIFICADA,
                null, null, null, null, "w * 0.5 + L * 0.1", "EUR");

        Ruta guardada = rutaService.crear(input);

        assertThat(guardada.getFormulaTarifa()).isEqualTo("w * 0.5 + L * 0.1");
        assertThat(guardada.getUnidadTarifa()).isEqualTo("EUR");
    }

    // ———————————————————————————————————————————
    // actualizar()
    // ———————————————————————————————————————————

    @Test
    @DisplayName("actualizar modifica nombre y estado correctamente")
    void actualizar_modificaNombreYEstado() {
        Ruta guardada = rutaService.crear(dto("Ruta Original", EstadoRuta.PLANIFICADA));

        RutaInputDTO cambios = new RutaInputDTO("Ruta Actualizada", null, EstadoRuta.EN_CURSO,
                null, null, 120.5, "obs", null, null);

        Ruta actualizada = rutaService.actualizar(guardada.getId(), cambios);

        assertThat(actualizada.getNombre()).isEqualTo("Ruta Actualizada");
        assertThat(actualizada.getEstado()).isEqualTo(EstadoRuta.EN_CURSO);
        assertThat(actualizada.getDistanciaKm()).isEqualTo(120.5);
        assertThat(actualizada.getObservaciones()).isEqualTo("obs");
    }

    @Test
    @DisplayName("actualizar con nombre vacío lanza IllegalArgumentException")
    void actualizar_sinNombre_lanzaExcepcion() {
        Ruta guardada = rutaService.crear(dto("Ruta Test", EstadoRuta.PLANIFICADA));

        RutaInputDTO cambios = dto("", EstadoRuta.PLANIFICADA);

        assertThatThrownBy(() -> rutaService.actualizar(guardada.getId(), cambios))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");
    }

    // ———————————————————————————————————————————
    // findByEstado()
    // ———————————————————————————————————————————

    @Test
    @DisplayName("findByEstado devuelve solo rutas con ese estado")
    void findByEstado_filtraCorrectamente() {
        rutaService.crear(dto("Planificada", EstadoRuta.PLANIFICADA));
        rutaService.crear(dto("Completada", EstadoRuta.COMPLETADA));

        var planificadas = rutaService.findByEstado(EstadoRuta.PLANIFICADA);

        assertThat(planificadas).isNotEmpty();
        assertThat(planificadas).allMatch(r -> r.getEstado() == EstadoRuta.PLANIFICADA);
    }

    // ———————————————————————————————————————————
    // eliminar()
    // ———————————————————————————————————————————

    @Test
    @DisplayName("eliminar borra la ruta del repositorio")
    void eliminar_borraLaRuta() {
        Ruta guardada = rutaService.crear(dto("Para borrar", EstadoRuta.PLANIFICADA));

        rutaService.eliminar(guardada.getId());

        assertThat(rutaRepo.findById(guardada.getId())).isEmpty();
    }
}
