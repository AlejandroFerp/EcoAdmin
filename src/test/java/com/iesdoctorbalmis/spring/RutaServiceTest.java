package com.iesdoctorbalmis.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.iesdoctorbalmis.spring.modelo.Ruta;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoRuta;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;
import com.iesdoctorbalmis.spring.repository.RutaRepository;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;
import com.iesdoctorbalmis.spring.servicios.RutaService;

@SpringBootTest
@Transactional
class RutaServiceTest {

    @Autowired private RutaService rutaService;
    @Autowired private RutaRepository rutaRepo;
    @Autowired private UsuarioRepository usuarioRepo;

    private Usuario crearTransportista(String suffix) {
        return usuarioRepo.save(new Usuario("Trans " + suffix, "trans" + suffix + "@test.com", "pass", Rol.TRANSPORTISTA));
    }

    // ———————————————————————————————————————————
    // crear()
    // ———————————————————————————————————————————

    @Test
    @DisplayName("crear ruta con nombre válido la persiste correctamente")
    void crear_conNombreValido_persiste() {
        Usuario trans = crearTransportista("A");
        Ruta datos = new Ruta();
        datos.setNombre("Ruta Norte");
        datos.setEstado(EstadoRuta.PLANIFICADA);

        Ruta guardada = rutaService.crear(datos, trans.getId());

        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getNombre()).isEqualTo("Ruta Norte");
        assertThat(guardada.getTransportista().getId()).isEqualTo(trans.getId());
    }

    @Test
    @DisplayName("crear ruta sin nombre lanza IllegalArgumentException")
    void crear_sinNombre_lanzaExcepcion() {
        Ruta datos = new Ruta();
        datos.setNombre("  ");
        datos.setEstado(EstadoRuta.PLANIFICADA);

        assertThatThrownBy(() -> rutaService.crear(datos, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");
    }

    @Test
    @DisplayName("crear ruta con fórmula inválida lanza IllegalArgumentException")
    void crear_conFormulaInvalida_lanzaExcepcion() {
        Ruta datos = new Ruta();
        datos.setNombre("Ruta Tarifas");
        datos.setEstado(EstadoRuta.PLANIFICADA);
        datos.setFormulaTarifa("w +++ L");  // sintaxis inválida para exp4j

        assertThatThrownBy(() -> rutaService.crear(datos, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("crear ruta con fórmula válida la persiste")
    void crear_conFormulaValida_persiste() {
        Ruta datos = new Ruta();
        datos.setNombre("Ruta con Tarifa");
        datos.setEstado(EstadoRuta.PLANIFICADA);
        datos.setFormulaTarifa("w * 0.5 + L * 0.1");
        datos.setUnidadTarifa("EUR");

        Ruta guardada = rutaService.crear(datos, null);

        assertThat(guardada.getFormulaTarifa()).isEqualTo("w * 0.5 + L * 0.1");
        assertThat(guardada.getUnidadTarifa()).isEqualTo("EUR");
    }

    // ———————————————————————————————————————————
    // actualizar()
    // ———————————————————————————————————————————

    @Test
    @DisplayName("actualizar establece las coordenadas lat/lon correctamente")
    void actualizar_estableceCoordenadas() {
        Ruta original = new Ruta();
        original.setNombre("Ruta Original");
        original.setEstado(EstadoRuta.PLANIFICADA);
        Ruta guardada = rutaService.crear(original, null);

        Ruta cambios = new Ruta();
        cambios.setNombre("Ruta Actualizada");
        cambios.setEstado(EstadoRuta.EN_CURSO);
        cambios.setOrigenLat(37.38634);
        cambios.setOrigenLon(-5.99295);
        cambios.setDestinoLat(36.72016);
        cambios.setDestinoLon(-4.42034);

        Ruta actualizada = rutaService.actualizar(guardada.getId(), cambios, null);

        assertThat(actualizada.getNombre()).isEqualTo("Ruta Actualizada");
        assertThat(actualizada.getEstado()).isEqualTo(EstadoRuta.EN_CURSO);
        assertThat(actualizada.getOrigenLat()).isEqualTo(37.38634);
        assertThat(actualizada.getOrigenLon()).isEqualTo(-5.99295);
        assertThat(actualizada.getDestinoLat()).isEqualTo(36.72016);
        assertThat(actualizada.getDestinoLon()).isEqualTo(-4.42034);
    }

    @Test
    @DisplayName("actualizar con nombre vacío lanza IllegalArgumentException")
    void actualizar_sinNombre_lanzaExcepcion() {
        Ruta original = new Ruta();
        original.setNombre("Ruta Test");
        original.setEstado(EstadoRuta.PLANIFICADA);
        Ruta guardada = rutaService.crear(original, null);

        Ruta cambios = new Ruta();
        cambios.setNombre("");
        cambios.setEstado(EstadoRuta.PLANIFICADA);

        assertThatThrownBy(() -> rutaService.actualizar(guardada.getId(), cambios, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");
    }

    // ———————————————————————————————————————————
    // findByEstado()
    // ———————————————————————————————————————————

    @Test
    @DisplayName("findByEstado devuelve solo rutas con ese estado")
    void findByEstado_filtraCorrectamente() {
        Ruta planificada = new Ruta();
        planificada.setNombre("Planificada");
        planificada.setEstado(EstadoRuta.PLANIFICADA);
        rutaService.crear(planificada, null);

        Ruta completada = new Ruta();
        completada.setNombre("Completada");
        completada.setEstado(EstadoRuta.COMPLETADA);
        rutaService.crear(completada, null);

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
        Ruta datos = new Ruta();
        datos.setNombre("Para borrar");
        datos.setEstado(EstadoRuta.PLANIFICADA);
        Ruta guardada = rutaService.crear(datos, null);

        rutaService.eliminar(guardada.getId());

        assertThat(rutaRepo.findById(guardada.getId())).isEmpty();
    }
}
