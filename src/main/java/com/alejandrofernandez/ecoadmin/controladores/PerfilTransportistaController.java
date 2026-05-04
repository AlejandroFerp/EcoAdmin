package com.alejandrofernandez.ecoadmin.controladores;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alejandrofernandez.ecoadmin.excepciones.AccesoDenegadoException;
import com.alejandrofernandez.ecoadmin.excepciones.RecursoNoEncontradoException;
import com.alejandrofernandez.ecoadmin.modelo.PerfilTransportista;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;
import com.alejandrofernandez.ecoadmin.repository.UsuarioRepository;
import com.alejandrofernandez.ecoadmin.servicios.PerfilTransportistaService;
import com.alejandrofernandez.ecoadmin.servicios.TarifaValidator;
import com.alejandrofernandez.ecoadmin.servicios.UsuarioAutenticadoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "PerfilTransportista", description = "Perfil profesional y tarifa del transportista")
@RestController
public class PerfilTransportistaController {

    private final PerfilTransportistaService service;
    private final TarifaValidator validator;
    private final UsuarioAutenticadoService authService;
    private final UsuarioRepository usuarioRepo;

    public PerfilTransportistaController(PerfilTransportistaService service,
                                         TarifaValidator validator,
                                         UsuarioAutenticadoService authService,
                                         UsuarioRepository usuarioRepo) {
        this.service = service;
        this.validator = validator;
        this.authService = authService;
        this.usuarioRepo = usuarioRepo;
    }

    @Operation(summary = "Listar todos los usuarios con rol TRANSPORTISTA")
    @GetMapping("/api/transportistas")
    public List<Usuario> listarTransportistas() {
        return usuarioRepo.findByRol(Rol.TRANSPORTISTA);
    }

    @Operation(summary = "Obtener perfil de transportista por ID de usuario")
    @GetMapping("/api/usuarios/{id}/perfil-transportista")
    public ResponseEntity<PerfilTransportista> obtener(@PathVariable Long id) {
        Optional<PerfilTransportista> perfil = service.findByUsuarioId(id);
        return perfil.map(ResponseEntity::ok)
                     .orElseThrow(() -> new RecursoNoEncontradoException("Perfil no encontrado para usuario: " + id));
    }

    @Operation(summary = "Crear o actualizar perfil de transportista")
    @PutMapping("/api/usuarios/{id}/perfil-transportista")
    public ResponseEntity<PerfilTransportista> guardar(@PathVariable Long id,
                                                        @RequestBody PerfilTransportista datos) {
        Usuario actual = authService.obtenerUsuarioActual();
        boolean esAdmin = authService.esAdmin(actual);
        boolean esPropioTransportista = actual != null
            && actual.getId().equals(id)
            && actual.getRol() == Rol.TRANSPORTISTA;

        if (!esAdmin && !esPropioTransportista) {
            throw new AccesoDenegadoException("No tiene permiso para modificar este perfil.");
        }

        try {
            PerfilTransportista saved = service.guardar(id, datos);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Operation(summary = "Calcular tarifa para un transportista dado peso y distancia")
    @GetMapping("/api/transportistas/{id}/calcular-tarifa")
    public ResponseEntity<Map<String, Object>> calcularTarifa(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") double w,
            @RequestParam(defaultValue = "0") double L) {

        PerfilTransportista perfil = service.findByUsuarioId(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Perfil no encontrado para transportista: " + id));

        String formula = perfil.getFormulaTarifa();
        if (formula == null || formula.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "El transportista no tiene formula de tarifa configurada."));
        }

        try {
            double resultado = validator.calcular(formula, w, L);
            return ResponseEntity.ok(Map.of(
                "resultado", Math.round(resultado * 100.0) / 100.0,
                "moneda", "EUR",
                "formula", formula,
                "w", w,
                "L", L
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al calcular tarifa: " + e.getMessage()));
        }
    }
}