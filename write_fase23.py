import os

base = r"C:\Users\afp5\Git\servidor_api\servidor_api\ServidorApiRest\src\main\java\com\iesdoctorbalmis\spring"

files = {}

files["servicios/TarifaValidator.java"] = r"""package com.iesdoctorbalmis.spring.servicios;

import java.util.regex.Pattern;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.stereotype.Component;

/** Valida formulas de tarifa. Variables: w (peso kg), L (distancia km). */
@Component
public class TarifaValidator {

    private static final Pattern WHITELIST = Pattern.compile("^[0-9wL\\s+\\-*/().]+$");
    private static final Pattern DOBLE_OP = Pattern.compile("[+*/][+\\-*/]");
    private static final Pattern DOBLE_VAL = Pattern.compile("[wL][wL0-9]|[0-9][wL]");

    public record ResultadoValidacion(boolean valido, String mensaje) {}

    public ResultadoValidacion validar(String formula) {
        if (formula == null || formula.isBlank())
            return new ResultadoValidacion(false, "La formula no puede estar vacia.");
        String f = formula.trim();
        if (!WHITELIST.matcher(f).matches())
            return new ResultadoValidacion(false, "Caracteres no permitidos. Solo: digitos, w, L, +, -, *, /, (, ), punto.");
        if (!f.contains("w") && !f.contains("L"))
            return new ResultadoValidacion(false, "La formula debe contener al menos w o L.");
        if (DOBLE_OP.matcher(f).find())
            return new ResultadoValidacion(false, "Operadores consecutivos no permitidos (p.ej. *+, /-).");
        if (DOBLE_VAL.matcher(f).find())
            return new ResultadoValidacion(false, "Dos valores seguidos sin operador (p.ej. wL, 2w).");
        try {
            double r = new ExpressionBuilder(f).variables("w","L").build()
                .setVariable("w",1.0).setVariable("L",1.0).evaluate();
            if (!Double.isFinite(r))
                return new ResultadoValidacion(false, "La formula produce un resultado no finito.");
        } catch (Exception e) {
            return new ResultadoValidacion(false, "Formula invalida: " + e.getMessage());
        }
        return new ResultadoValidacion(true, "OK");
    }

    public double calcular(String formula, double w, double L) {
        return new ExpressionBuilder(formula).variables("w","L").build()
            .setVariable("w", w).setVariable("L", L).evaluate();
    }
}
"""

files["servicios/PerfilTransportistaService.java"] = r"""package com.iesdoctorbalmis.spring.servicios;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iesdoctorbalmis.spring.excepciones.RecursoNoEncontradoException;
import com.iesdoctorbalmis.spring.modelo.PerfilTransportista;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;
import com.iesdoctorbalmis.spring.repository.PerfilTransportistaRepository;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;

@Service
public class PerfilTransportistaService {

    private final PerfilTransportistaRepository repo;
    private final UsuarioRepository usuarioRepo;
    private final TarifaValidator validator;

    public PerfilTransportistaService(PerfilTransportistaRepository repo,
                                      UsuarioRepository usuarioRepo,
                                      TarifaValidator validator) {
        this.repo = repo;
        this.usuarioRepo = usuarioRepo;
        this.validator = validator;
    }

    public Optional<PerfilTransportista> findByUsuarioId(Long usuarioId) {
        return usuarioRepo.findById(usuarioId)
            .flatMap(repo::findByUsuario);
    }

    @Transactional
    public PerfilTransportista guardar(Long usuarioId, PerfilTransportista datos) {
        Usuario usuario = usuarioRepo.findById(usuarioId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + usuarioId));

        if (usuario.getRol() != Rol.TRANSPORTISTA) {
            throw new IllegalArgumentException("El usuario no tiene rol TRANSPORTISTA.");
        }

        if (datos.getFormulaTarifa() != null && !datos.getFormulaTarifa().isBlank()) {
            TarifaValidator.ResultadoValidacion rv = validator.validar(datos.getFormulaTarifa());
            if (!rv.valido()) {
                throw new IllegalArgumentException(rv.mensaje());
            }
        }

        PerfilTransportista perfil = repo.findByUsuario(usuario).orElse(new PerfilTransportista());
        perfil.setUsuario(usuario);
        perfil.setMatricula(datos.getMatricula());
        perfil.setFormulaTarifa(datos.getFormulaTarifa());
        perfil.setUnidadTarifa(datos.getUnidadTarifa());
        perfil.setObservaciones(datos.getObservaciones());

        return repo.save(perfil);
    }
}
"""

files["controladores/PerfilTransportistaController.java"] = r"""package com.iesdoctorbalmis.spring.controladores;

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

import com.iesdoctorbalmis.spring.excepciones.AccesoDenegadoException;
import com.iesdoctorbalmis.spring.excepciones.RecursoNoEncontradoException;
import com.iesdoctorbalmis.spring.modelo.PerfilTransportista;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;
import com.iesdoctorbalmis.spring.servicios.PerfilTransportistaService;
import com.iesdoctorbalmis.spring.servicios.TarifaValidator;
import com.iesdoctorbalmis.spring.servicios.UsuarioAutenticadoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "PerfilTransportista", description = "Perfil profesional y tarifa del transportista")
@RestController
public class PerfilTransportistaController {

    private final PerfilTransportistaService service;
    private final TarifaValidator validator;
    private final UsuarioAutenticadoService authService;

    public PerfilTransportistaController(PerfilTransportistaService service,
                                         TarifaValidator validator,
                                         UsuarioAutenticadoService authService) {
        this.service = service;
        this.validator = validator;
        this.authService = authService;
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
            return ResponseEntity.badRequest()
                .body(null);
        }
    }

    @Operation(summary = "Calcular tarifa para un transportista dado peso y distancia")
    @GetMapping("/api/transportistas/{id}/calcular-tarifa")
    public ResponseEntity<Map<String, Object>> calcularTarifa(
            @PathVariable Long id,
            @RequestParam double w,
            @RequestParam double L) {

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
"""

for rel_path, content in files.items():
    full_path = os.path.join(base, rel_path.replace("/", os.sep))
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, "w", encoding="utf-8", newline="\n") as f:
        f.write(content)
    print(f"OK: {rel_path}")
