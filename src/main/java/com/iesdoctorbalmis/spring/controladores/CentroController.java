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
import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.TrasladoRepository;
import com.iesdoctorbalmis.spring.servicios.CentroService;
import com.iesdoctorbalmis.spring.servicios.UsuarioAutenticadoService;

@RestController
@RequestMapping("/api/centros")
public class CentroController {

    private final CentroService service;
    private final UsuarioAutenticadoService authService;
    private final ResiduoRepository residuoRepo;
    private final TrasladoRepository trasladoRepo;

    public CentroController(CentroService service, UsuarioAutenticadoService authService,
                            ResiduoRepository residuoRepo, TrasladoRepository trasladoRepo) {
        this.service = service;
        this.authService = authService;
        this.residuoRepo = residuoRepo;
        this.trasladoRepo = trasladoRepo;
    }

    @GetMapping
    public List<Centro> listar() {
        Usuario usuario = authService.obtenerUsuarioActual();
        if (usuario == null) return List.of();

        if (authService.esAdmin(usuario) || usuario.getRol() == Rol.GESTOR) {
            return service.findAll();
        }
        return service.findByUsuario(usuario);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Centro> buscar(@PathVariable Long id) {
        Centro c = service.findById(id);
        if (c == null) throw new RecursoNoEncontradoException("Centro no encontrado: " + id);
        verificarAccesoCentro(c);
        return ResponseEntity.ok(c);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'PRODUCTOR')")
    public ResponseEntity<Centro> crear(@RequestBody Centro c) {
        Usuario usuario = authService.obtenerUsuarioActual();
        if (usuario != null && c.getUsuario() == null) {
            c.setUsuario(usuario);
        }
        Centro saved = service.save(c);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Centro> editar(@PathVariable Long id, @RequestBody Centro c) {
        Centro existing = service.findById(id);
        if (existing == null) throw new RecursoNoEncontradoException("Centro no encontrado: " + id);
        verificarAccesoCentro(existing);
        c.setId(id);
        c.setUsuario(existing.getUsuario());
        return ResponseEntity.ok(service.save(c));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        Centro c = service.findById(id);
        if (c == null) throw new RecursoNoEncontradoException("Centro no encontrado: " + id);
        verificarAccesoCentro(c);

        if (residuoRepo.existsByCentro(c)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(java.util.Map.of("error",
                    "No se puede eliminar el centro '" + c.getNombre() + "': tiene residuos asociados."));
        }
        if (trasladoRepo.existsByCentroProductorOrCentroGestor(c, c)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(java.util.Map.of("error",
                    "No se puede eliminar el centro '" + c.getNombre() + "': tiene traslados asociados."));
        }

        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void verificarAccesoCentro(Centro centro) {
        Usuario usuario = authService.obtenerUsuarioActual();
        if (usuario == null) throw new AccesoDenegadoException("No autenticado");
        if (authService.esAdmin(usuario) || usuario.getRol() == Rol.GESTOR) return;

        if (centro.getUsuario() == null || !centro.getUsuario().getId().equals(usuario.getId())) {
            throw new AccesoDenegadoException("No tiene acceso a este centro");
        }
    }
}