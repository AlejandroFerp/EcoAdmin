package com.alejandrofernandez.ecoadmin.controladores;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
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

import com.alejandrofernandez.ecoadmin.dto.RutaInputDTO;
import com.alejandrofernandez.ecoadmin.dto.RutaTransportistaInputDTO;
import com.alejandrofernandez.ecoadmin.dto.RutaTransportistaViewDTO;
import com.alejandrofernandez.ecoadmin.excepciones.AccesoDenegadoException;
import com.alejandrofernandez.ecoadmin.excepciones.RecursoNoEncontradoException;
import com.alejandrofernandez.ecoadmin.modelo.Ruta;
import com.alejandrofernandez.ecoadmin.modelo.RutaTransportista;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoRuta;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;
import com.alejandrofernandez.ecoadmin.servicios.RutaService;
import com.alejandrofernandez.ecoadmin.servicios.RutaTransportistaService;
import com.alejandrofernandez.ecoadmin.servicios.TarifaValidator;
import com.alejandrofernandez.ecoadmin.servicios.UsuarioAutenticadoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Rutas", description = "Planificacion y seguimiento de rutas de recogida")
@RestController
@RequestMapping("/api/rutas")
public class RutaController {

    private final RutaService rutaService;
    private final UsuarioAutenticadoService authService;
    private final TarifaValidator validator;
    private final RutaTransportistaService rtService;

    public RutaController(RutaService rutaService, UsuarioAutenticadoService authService,
            TarifaValidator validator, RutaTransportistaService rtService) {
        this.rutaService = rutaService;
        this.authService = authService;
        this.validator = validator;
        this.rtService = rtService;
    }

    @Operation(summary = "Listar rutas (admin/gestor: todas o filtradas; transportista: las suyas)")
    @GetMapping
    public List<Ruta> listar(@RequestParam(required = false) Long transportistaId) {
        Usuario actual = authService.obtenerUsuarioActual();
        if (actual != null && actual.getRol() == Rol.TRANSPORTISTA) {
            // Rutas donde el transportista está activamente asignado (modelo M:N)
            return rtService.getRutasPorTransportista(actual.getId());
        }
        if (transportistaId != null) {
            return rtService.getRutasPorTransportista(transportistaId);
        }
        return rutaService.findAll();
    }

    @Operation(summary = "Rutas activas (PLANIFICADA + EN_CURSO) para el mapa")
    @GetMapping("/activas")
    public List<Ruta> activas() {
        List<Ruta> planificadas = rutaService.findByEstado(EstadoRuta.PLANIFICADA);
        List<Ruta> enCurso = rutaService.findByEstado(EstadoRuta.EN_CURSO);
        java.util.List<Ruta> todas = new java.util.ArrayList<>(planificadas);
        todas.addAll(enCurso);
        return todas;
    }

    @Operation(summary = "Crear nueva ruta (admin/gestor)")
    @PostMapping
    public ResponseEntity<Ruta> crear(@RequestBody RutaInputDTO dto) {
        Usuario actual = authService.obtenerUsuarioActual();
        if (actual == null || (actual.getRol() != Rol.ADMIN && actual.getRol() != Rol.GESTOR))
            throw new AccesoDenegadoException("Solo ADMIN o GESTOR pueden crear rutas.");
        return ResponseEntity.ok(rutaService.crear(dto));
    }

    @Operation(summary = "Actualizar ruta")
    @PutMapping("/{id}")
    public ResponseEntity<Ruta> actualizar(@PathVariable Long id, @RequestBody RutaInputDTO dto) {
        Usuario actual = authService.obtenerUsuarioActual();
        Ruta existente = rutaService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada: " + id));
        boolean esAdminGestor = actual != null && (actual.getRol() == Rol.ADMIN || actual.getRol() == Rol.GESTOR);
        boolean esPropioTransportista = actual != null && actual.getRol() == Rol.TRANSPORTISTA
                && rtService.perteneceARuta(id, actual.getId());
        if (!esAdminGestor && !esPropioTransportista)
            throw new AccesoDenegadoException("No tiene permiso para modificar esta ruta.");

        // In order to only update provided fields, if DTO field is null, we can keep
        // the existing one.
        // For simplicity, we assume the frontend sends the whole object or we construct
        // a new DTO merging them.
        RutaInputDTO merged = new RutaInputDTO(
                dto.nombre() != null ? dto.nombre() : existente.getNombre(),
                dto.fecha() != null ? dto.fecha() : existente.getFecha(),
                dto.estado() != null ? dto.estado() : existente.getEstado(),
                dto.origenId() != null ? dto.origenId()
                        : (existente.getOrigen() != null ? existente.getOrigen().getId() : null),
                dto.destinoId() != null ? dto.destinoId()
                        : (existente.getDestino() != null ? existente.getDestino().getId() : null),
                dto.distanciaKm() != null ? dto.distanciaKm() : existente.getDistanciaKm(),
                dto.observaciones() != null ? dto.observaciones() : existente.getObservaciones(),
                dto.formulaTarifa() != null ? dto.formulaTarifa() : existente.getFormulaTarifa(),
                dto.unidadTarifa() != null ? dto.unidadTarifa() : existente.getUnidadTarifa());

        return ResponseEntity.ok(rutaService.actualizar(id, merged));
    }

    @Operation(summary = "Calcular tarifa de una ruta especifica")
    @GetMapping("/{id}/calcular")
    public ResponseEntity<?> calcular(@PathVariable Long id,
            @RequestParam(defaultValue = "0") double w,
            @RequestParam(defaultValue = "0") double L,
            @RequestParam(required = false) String formula) {
        Ruta ruta = rutaService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada: " + id));
        String formulaActiva = formula != null ? formula : ruta.getFormulaTarifa();
        if (formulaActiva == null || formulaActiva.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Esta ruta no tiene formula de tarifa definida."));
        }
        TarifaValidator.ResultadoValidacion validacion = validator.validar(formulaActiva);
        if (!validacion.valido()) {
            return ResponseEntity.badRequest().body(Map.of("error", validacion.mensaje()));
        }
        try {
            double resultado = validator.calcular(formulaActiva, w, L);
            if (!Double.isFinite(resultado)) {
                return ResponseEntity.badRequest().body(Map.of("error", "La formula produce un resultado no finito."));
            }
            return ResponseEntity.ok(Map.of(
                    "formula", formulaActiva,
                    "w", w, "L", L,
                    "resultado", Math.round(resultado * 100.0) / 100.0,
                    "moneda", ruta.getUnidadTarifa() != null ? ruta.getUnidadTarifa() : "EUR"));
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

    // ============================================================
    // Transportistas por ruta (M:N)
    // ============================================================

    @Operation(summary = "Lista transportistas activos de una ruta con tarifa y precio de ejemplo")
    @GetMapping("/{id}/transportistas")
    public List<RutaTransportistaViewDTO> listarTransportistas(@PathVariable Long id) {
        return rtService.listarConPrecio(id);
    }

    @Operation(summary = "Asignar transportista a una ruta con su fórmula de tarifa (admin/gestor)")
    @PostMapping("/{id}/transportistas")
    public ResponseEntity<RutaTransportista> asignarTransportista(
            @PathVariable Long id, @RequestBody RutaTransportistaInputDTO dto) {
        requireAdminGestor();
        RutaTransportista rt = rtService.asignar(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(rt);
    }

    @Operation(summary = "Actualizar fórmula/moneda del transportista en la ruta (admin/gestor)")
    @PutMapping("/{id}/transportistas/{transId}")
    public ResponseEntity<RutaTransportista> actualizarTransportista(
            @PathVariable Long id, @PathVariable Long transId,
            @RequestBody RutaTransportistaInputDTO dto) {
        requireAdminGestor();
        return ResponseEntity.ok(rtService.actualizar(id, transId, dto));
    }

    @Operation(summary = "Desasignar transportista de una ruta (admin/gestor)")
    @DeleteMapping("/{id}/transportistas/{transId}")
    public ResponseEntity<Void> desasignarTransportista(
            @PathVariable Long id, @PathVariable Long transId) {
        requireAdminGestor();
        rtService.desasignar(id, transId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Calcular precio para transportista en ruta dado el peso del residuo")
    @GetMapping("/{id}/calcular/{transId}")
    public ResponseEntity<?> calcularPorTransportista(
            @PathVariable Long id, @PathVariable Long transId,
            @RequestParam(defaultValue = "0") double w,
            @RequestParam(required = false) Double L,
            @RequestParam(required = false) String formula) {
        Map<String, Object> resultado = rtService.calcularPrecio(id, transId, w, L, formula);
        if (resultado.containsKey("error")) {
            return ResponseEntity.badRequest().body(resultado);
        }
        return ResponseEntity.ok(resultado);
    }

    private void requireAdminGestor() {
        Usuario actual = authService.obtenerUsuarioActual();
        if (actual == null || (actual.getRol() != Rol.ADMIN && actual.getRol() != Rol.GESTOR))
            throw new AccesoDenegadoException("Solo ADMIN o GESTOR pueden gestionar transportistas de rutas.");
    }
}