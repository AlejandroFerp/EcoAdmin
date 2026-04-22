package com.iesdoctorbalmis.spring.controladores;

import java.time.LocalDate;
import java.util.HashMap;
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

import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Recogida;
import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoRecogida;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;
import com.iesdoctorbalmis.spring.servicios.RecogidaService;
import com.iesdoctorbalmis.spring.servicios.UsuarioAutenticadoService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Recogidas", description = "Gestion de recogidas programadas")
@RestController
@RequestMapping("/api/recogidas")
public class RecogidaController {

    private final RecogidaService service;
    private final UsuarioAutenticadoService authService;
    private final ResiduoRepository residuoRepo;
    private final CentroRepository centroRepo;
    private final UsuarioRepository usuarioRepo;

    public RecogidaController(RecogidaService service,
                              UsuarioAutenticadoService authService,
                              ResiduoRepository residuoRepo,
                              CentroRepository centroRepo,
                              UsuarioRepository usuarioRepo) {
        this.service = service;
        this.authService = authService;
        this.residuoRepo = residuoRepo;
        this.centroRepo = centroRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @GetMapping
    public List<Map<String, Object>> listar(@RequestParam(required = false) String estado,
                                            @RequestParam(required = false) String desde,
                                            @RequestParam(required = false) String hasta) {
        List<Recogida> base = visibles();
        if (estado != null && !estado.isBlank()) {
            EstadoRecogida e = EstadoRecogida.valueOf(estado.toUpperCase());
            base = base.stream().filter(r -> r.getEstado() == e).toList();
        }
        if (desde != null && !desde.isBlank()) {
            LocalDate d = LocalDate.parse(desde);
            base = base.stream().filter(r -> r.getFechaProgramada() != null && !r.getFechaProgramada().isBefore(d)).toList();
        }
        if (hasta != null && !hasta.isBlank()) {
            LocalDate h = LocalDate.parse(hasta);
            base = base.stream().filter(r -> r.getFechaProgramada() != null && !r.getFechaProgramada().isAfter(h)).toList();
        }
        return base.stream().map(RecogidaController::toDto).toList();
    }

    @GetMapping("/{id}")
    public Map<String, Object> obtener(@PathVariable Long id) {
        return toDto(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestBody Map<String, Object> body) {
        Recogida r = new Recogida();
        aplicar(r, body);
        Recogida guardado = service.save(r);
        return ResponseEntity.ok(toDto(guardado));
    }

    @PutMapping("/{id}")
    public Map<String, Object> actualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Recogida r = service.findById(id);
        aplicar(r, body);
        return toDto(service.save(r));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ===== helpers =====

    private List<Recogida> visibles() {
        Usuario u = authService.obtenerUsuarioActual();
        if (u == null) return List.of();
        if (authService.esAdmin(u) || u.getRol() == Rol.GESTOR) {
            return service.findAll();
        }
        if (u.getRol() == Rol.PRODUCTOR) {
            List<Centro> centros = centroRepo.findByUsuario(u);
            return service.findByCentros(centros);
        }
        // TRANSPORTISTA: solo asignadas a el
        return service.findAll().stream()
                .filter(r -> r.getTransportista() != null && r.getTransportista().getId().equals(u.getId()))
                .toList();
    }

    private void aplicar(Recogida r, Map<String, Object> body) {
        if (body.containsKey("residuoId")) {
            Object v = body.get("residuoId");
            r.setResiduo(v == null ? null : residuoRepo.findById(asLong(v)).orElse(null));
        }
        if (body.containsKey("centroOrigenId")) {
            Object v = body.get("centroOrigenId");
            r.setCentroOrigen(v == null ? null : centroRepo.findById(asLong(v)).orElse(null));
        }
        if (body.containsKey("centroDestinoId")) {
            Object v = body.get("centroDestinoId");
            r.setCentroDestino(v == null ? null : centroRepo.findById(asLong(v)).orElse(null));
        }
        if (body.containsKey("transportistaId")) {
            Object v = body.get("transportistaId");
            r.setTransportista(v == null ? null : usuarioRepo.findById(asLong(v)).orElse(null));
        }
        if (body.containsKey("fechaProgramada")) {
            Object v = body.get("fechaProgramada");
            r.setFechaProgramada(v == null || v.toString().isBlank() ? null : LocalDate.parse(v.toString()));
        }
        if (body.containsKey("fechaRealizada")) {
            Object v = body.get("fechaRealizada");
            r.setFechaRealizada(v == null || v.toString().isBlank() ? null : LocalDate.parse(v.toString()));
        }
        if (body.containsKey("estado")) {
            Object v = body.get("estado");
            if (v != null && !v.toString().isBlank()) {
                r.setEstado(EstadoRecogida.valueOf(v.toString().toUpperCase()));
            }
        }
        if (body.containsKey("observaciones")) {
            Object v = body.get("observaciones");
            r.setObservaciones(v == null ? null : v.toString());
        }
    }

    private static long asLong(Object v) {
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(v.toString());
    }

    private static Map<String, Object> toDto(Recogida r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId());
        m.put("estado", r.getEstado() == null ? null : r.getEstado().name());
        m.put("fechaProgramada", r.getFechaProgramada() == null ? null : r.getFechaProgramada().toString());
        m.put("fechaRealizada", r.getFechaRealizada() == null ? null : r.getFechaRealizada().toString());
        m.put("observaciones", r.getObservaciones());
        Residuo res = r.getResiduo();
        if (res != null) {
            m.put("residuoId", res.getId());
            m.put("residuoCodigoLER", res.getCodigoLER());
            m.put("residuoDescripcion", res.getDescripcion());
        }
        Centro co = r.getCentroOrigen();
        if (co != null) {
            m.put("centroOrigenId", co.getId());
            m.put("centroOrigenNombre", co.getNombre());
        }
        Centro cd = r.getCentroDestino();
        if (cd != null) {
            m.put("centroDestinoId", cd.getId());
            m.put("centroDestinoNombre", cd.getNombre());
        }
        Usuario t = r.getTransportista();
        if (t != null) {
            m.put("transportistaId", t.getId());
            m.put("transportistaNombre", t.getNombre());
        }
        return m;
    }
}
