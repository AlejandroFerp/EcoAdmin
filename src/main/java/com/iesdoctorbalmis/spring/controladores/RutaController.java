package com.iesdoctorbalmis.spring.controladores;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iesdoctorbalmis.spring.dto.RutaInputDTO;
import com.iesdoctorbalmis.spring.excepciones.AccesoDenegadoException;
import com.iesdoctorbalmis.spring.excepciones.RecursoNoEncontradoException;
import com.iesdoctorbalmis.spring.modelo.Ruta;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;
import com.iesdoctorbalmis.spring.servicios.RutaService;
import com.iesdoctorbalmis.spring.servicios.TarifaValidator;
import com.iesdoctorbalmis.spring.servicios.UsuarioAutenticadoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Rutas", description = "Planificacion y seguimiento de rutas de recogida")
@RestController
@RequestMapping("/api/rutas")
public class RutaController {

    private final RutaService rutaService;
    private final UsuarioAutenticadoService authService;
    private final TarifaValidator validator;

    public RutaController(RutaService rutaService, UsuarioAutenticadoService authService, TarifaValidator validator) {
        this.rutaService = rutaService;
        this.authService = authService;
        this.validator = validator;
    }

    @Operation(summary = "Listar rutas (admin/gestor: todas o filtradas; transportista: las suyas)")
    @GetMapping
    public List<Ruta> listar(@RequestParam(required = false) Long transportistaId) {
        Usuario actual = authService.obtenerUsuarioActual();
        if (actual != null && actual.getRol() == Rol.TRANSPORTISTA) {
            return rutaService.findByTransportistaId(actual.getId());
        }
        if (transportistaId != null) {
            return rutaService.findByTransportistaId(transportistaId);
        }
        return rutaService.findAll();
    }

    @Operation(summary = "Crear nueva ruta (admin/gestor)")
    @PostMapping
    public ResponseEntity<Ruta> crear(@RequestBody RutaInputDTO dto) {
        Usuario actual = authService.obtenerUsuarioActual();
        if (actual == null || (actual.getRol() != Rol.ADMIN && actual.getRol() != Rol.GESTOR))
            throw new AccesoDenegadoException("Solo ADMIN o GESTOR pueden crear rutas.");
        Ruta r = new Ruta();
        r.setNombre(dto.nombre());
        r.setFecha(dto.fecha());
        r.setEstado(dto.estado());
        r.setOrigenDireccion(dto.origenDireccion());
        r.setDestinoDireccion(dto.destinoDireccion());
        r.setDistanciaKm(dto.distanciaKm());
        r.setObservaciones(dto.observaciones());
        r.setFormulaTarifa(dto.formulaTarifa());
        r.setUnidadTarifa(dto.unidadTarifa());
        return ResponseEntity.ok(rutaService.crear(r, dto.transportistaId()));
    }

    @Operation(summary = "Actualizar ruta")
    @PutMapping("/{id}")
    public ResponseEntity<Ruta> actualizar(@PathVariable Long id, @RequestBody RutaInputDTO dto) {
        Usuario actual = authService.obtenerUsuarioActual();
        Ruta existente = rutaService.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada: " + id));
        boolean esAdminGestor = actual != null && (actual.getRol() == Rol.ADMIN || actual.getRol() == Rol.GESTOR);
        boolean esPropioTransportista = actual != null && actual.getRol() == Rol.TRANSPORTISTA
            && existente.getTransportista() != null
            && existente.getTransportista().getId().equals(actual.getId());
        if (!esAdminGestor && !esPropioTransportista)
            throw new AccesoDenegadoException("No tiene permiso para modificar esta ruta.");
        Ruta r = new Ruta();
        r.setNombre(dto.nombre() != null ? dto.nombre() : existente.getNombre());
        r.setFecha(dto.fecha() != null ? dto.fecha() : existente.getFecha());
        r.setEstado(dto.estado() != null ? dto.estado() : existente.getEstado());
        r.setOrigenDireccion(dto.origenDireccion() != null ? dto.origenDireccion() : existente.getOrigenDireccion());
        r.setDestinoDireccion(dto.destinoDireccion() != null ? dto.destinoDireccion() : existente.getDestinoDireccion());
        r.setDistanciaKm(dto.distanciaKm() != null ? dto.distanciaKm() : existente.getDistanciaKm());
        r.setObservaciones(dto.observaciones() != null ? dto.observaciones() : existente.getObservaciones());
        r.setFormulaTarifa(dto.formulaTarifa());
        r.setUnidadTarifa(dto.unidadTarifa());
        Long tId = esAdminGestor ? dto.transportistaId()
                   : (existente.getTransportista() != null ? existente.getTransportista().getId() : null);
        return ResponseEntity.ok(rutaService.actualizar(id, r, tId));
    }

    @Operation(summary = "Calcular tarifa de una ruta especifica")
    @GetMapping("/{id}/calcular")
    public ResponseEntity<?> calcular(@PathVariable Long id,
                                       @RequestParam(defaultValue = "0") double w,
                                       @RequestParam(defaultValue = "0") double L) {
        Ruta ruta = rutaService.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada: " + id));
        if (ruta.getFormulaTarifa() == null || ruta.getFormulaTarifa().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Esta ruta no tiene formula de tarifa definida."));
        }
        try {
            double resultado = validator.calcular(ruta.getFormulaTarifa(), w, L);
            if (!Double.isFinite(resultado)) {
                return ResponseEntity.badRequest().body(Map.of("error", "La formula produce un resultado no finito."));
            }
            return ResponseEntity.ok(Map.of(
                "formula", ruta.getFormulaTarifa(),
                "w", w, "L", L,
                "resultado", Math.round(resultado * 100.0) / 100.0,
                "moneda", ruta.getUnidadTarifa() != null ? ruta.getUnidadTarifa() : "EUR"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error en la formula: " + e.getMessage()));
        }
    }

    @Operation(summary = "Eliminar ruta (admin/gestor)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Usuario actual = authService.obtenerUsuarioActual();
        if (actual == null || (actual.getRol() != Rol.ADMIN && actual.getRol() != Rol.GESTOR))
            throw new AccesoDenegadoException("Solo ADMIN o GESTOR pueden eliminar rutas.");
        rutaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}