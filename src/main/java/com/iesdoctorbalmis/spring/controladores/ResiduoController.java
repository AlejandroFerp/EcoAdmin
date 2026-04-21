package com.iesdoctorbalmis.spring.controladores;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iesdoctorbalmis.spring.excepciones.AccesoDenegadoException;
import com.iesdoctorbalmis.spring.excepciones.RecursoNoEncontradoException;
import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;
import com.iesdoctorbalmis.spring.repository.TrasladoRepository;
import com.iesdoctorbalmis.spring.servicios.ResiduoService;
import com.iesdoctorbalmis.spring.servicios.UsuarioAutenticadoService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Residuos", description = "Catalogo de residuos peligrosos con codigos LER")
@RestController
@RequestMapping("/api/residuos")
public class ResiduoController {

    private final ResiduoService service;
    private final UsuarioAutenticadoService authService;
    private final TrasladoRepository trasladoRepo;

    public ResiduoController(ResiduoService service, UsuarioAutenticadoService authService,
                             TrasladoRepository trasladoRepo) {
        this.service = service;
        this.authService = authService;
        this.trasladoRepo = trasladoRepo;
    }

    @GetMapping
    public List<Residuo> listar() {
        Usuario usuario = authService.obtenerUsuarioActual();
        if (usuario == null) return List.of();

        if (authService.esAdmin(usuario) || usuario.getRol() == Rol.GESTOR) {
            return service.findAll();
        }
        return service.findByUsuario(usuario);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Residuo> buscar(@PathVariable Long id) {
        Residuo r = service.findById(id);
        if (r == null) throw new RecursoNoEncontradoException("Residuo no encontrado: " + id);
        verificarAccesoResiduo(r);
        return ResponseEntity.ok(r);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'PRODUCTOR')")
    public ResponseEntity<Residuo> crear(@RequestBody Residuo r) {
        Residuo saved = service.save(r);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Residuo> editar(@PathVariable Long id, @RequestBody Residuo r) {
        Residuo existing = service.findById(id);
        if (existing == null) throw new RecursoNoEncontradoException("Residuo no encontrado: " + id);
        verificarAccesoResiduo(existing);
        r.setId(id);
        r.setCentro(existing.getCentro());
        return ResponseEntity.ok(service.save(r));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        Residuo r = service.findById(id);
        if (r == null) throw new RecursoNoEncontradoException("Residuo no encontrado: " + id);
        verificarAccesoResiduo(r);

        if (trasladoRepo.existsByResiduo(r)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(java.util.Map.of("error",
                    "No se puede eliminar: el residuo '" + r.getCodigoLER() + "' tiene traslados asociados."));
        }

        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void verificarAccesoResiduo(Residuo residuo) {
        Usuario usuario = authService.obtenerUsuarioActual();
        if (usuario == null) throw new AccesoDenegadoException("No autenticado");
        if (authService.esAdmin(usuario) || usuario.getRol() == Rol.GESTOR) return;

        if (residuo.getCentro() == null || residuo.getCentro().getUsuario() == null
                || !residuo.getCentro().getUsuario().getId().equals(usuario.getId())) {
            throw new AccesoDenegadoException("No tiene acceso a este residuo");
        }
    }
}