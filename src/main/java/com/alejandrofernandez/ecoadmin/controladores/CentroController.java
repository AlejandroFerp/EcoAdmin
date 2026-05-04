package com.alejandrofernandez.ecoadmin.controladores;

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

import com.alejandrofernandez.ecoadmin.dto.CentroDTO;
import com.alejandrofernandez.ecoadmin.dto.CentroInputDTO;
import com.alejandrofernandez.ecoadmin.excepciones.AccesoDenegadoException;
import com.alejandrofernandez.ecoadmin.excepciones.RecursoNoEncontradoException;
import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.Direccion;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;
import com.alejandrofernandez.ecoadmin.repository.DireccionRepository;
import com.alejandrofernandez.ecoadmin.repository.ResiduoRepository;
import com.alejandrofernandez.ecoadmin.repository.TrasladoRepository;
import com.alejandrofernandez.ecoadmin.servicios.CentroService;
import com.alejandrofernandez.ecoadmin.servicios.OwnershipService;
import com.alejandrofernandez.ecoadmin.servicios.UsuarioAutenticadoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Centros", description = "Gestion de centros productores, gestores y transportistas")
@RestController
@RequestMapping("/api/centros")
public class CentroController {

    private final CentroService service;
    private final UsuarioAutenticadoService authService;
    private final OwnershipService ownershipService;
    private final ResiduoRepository residuoRepo;
    private final TrasladoRepository trasladoRepo;
    private final DireccionRepository direccionRepo;

    public CentroController(CentroService service, UsuarioAutenticadoService authService,
                            OwnershipService ownershipService,
                            ResiduoRepository residuoRepo, TrasladoRepository trasladoRepo,
                            DireccionRepository direccionRepo) {
        this.service = service;
        this.authService = authService;
        this.ownershipService = ownershipService;
        this.residuoRepo = residuoRepo;
        this.trasladoRepo = trasladoRepo;
        this.direccionRepo = direccionRepo;
    }

    @GetMapping
    public List<CentroDTO> listar() {
        Usuario usuario = authService.obtenerUsuarioActual();
        if (usuario == null) return List.of();
        return service.findAllForUsuario(usuario).stream().map(CentroDTO::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CentroDTO> buscar(@PathVariable Long id) {
        Centro c = service.findById(id);
        if (c == null) throw new RecursoNoEncontradoException("Centro no encontrado: " + id);
        verificarAccesoCentro(c);
        return ResponseEntity.ok(CentroDTO.from(c));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'PRODUCTOR')")
    public ResponseEntity<CentroDTO> crear(@RequestBody CentroInputDTO input) {
        Usuario usuario = authService.obtenerUsuarioActual();
        Centro c = mapInputToEntity(input);
        if (usuario != null) {
            c.setUsuario(usuario);
        }
        Centro saved = service.save(c);
        return ResponseEntity.status(HttpStatus.CREATED).body(CentroDTO.from(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CentroDTO> editar(@PathVariable Long id, @RequestBody CentroInputDTO input) {
        Centro existing = service.findById(id);
        if (existing == null) throw new RecursoNoEncontradoException("Centro no encontrado: " + id);
        verificarAccesoCentro(existing);

        Centro c = mapInputToEntity(input);
        c.setId(id);
        c.setUsuario(existing.getUsuario());
        return ResponseEntity.ok(CentroDTO.from(service.save(c)));
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

    private Centro mapInputToEntity(CentroInputDTO input) {
        Centro c = new Centro();
        c.setNombre(input.nombre());
        c.setTipo(input.tipo());
        c.setNima(input.nima());
        c.setTelefono(input.telefono());
        c.setEmail(input.email());
        c.setNombreContacto(input.nombreContacto());
        c.setDetalleEnvio(input.detalleEnvio());

        if (input.direccionId() != null) {
            Direccion dir = direccionRepo.findById(input.direccionId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Direccion no encontrada: " + input.direccionId()));
            c.setDireccion(dir);
        }
        return c;
    }

    private void verificarAccesoCentro(Centro centro) {
        Usuario usuario = authService.obtenerUsuarioActual();
        if (usuario == null) throw new AccesoDenegadoException("No autenticado");
        if (!ownershipService.canAccessCentro(usuario, centro.getId())) {
            throw new AccesoDenegadoException("No tiene acceso a este centro");
        }
    }
}