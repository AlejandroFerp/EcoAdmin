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

import com.alejandrofernandez.ecoadmin.dto.ResiduoDTO;
import com.alejandrofernandez.ecoadmin.dto.ResiduoInputDTO;
import com.alejandrofernandez.ecoadmin.excepciones.AccesoDenegadoException;
import com.alejandrofernandez.ecoadmin.excepciones.RecursoNoEncontradoException;
import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.Residuo;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;
import com.alejandrofernandez.ecoadmin.repository.TrasladoRepository;
import com.alejandrofernandez.ecoadmin.servicios.CentroService;
import com.alejandrofernandez.ecoadmin.servicios.OwnershipService;
import com.alejandrofernandez.ecoadmin.servicios.ResiduoService;
import com.alejandrofernandez.ecoadmin.servicios.UsuarioAutenticadoService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Residuos", description = "Catalogo de residuos peligrosos con codigos LER")
@RestController
@RequestMapping("/api/residuos")
public class ResiduoController {

    private final ResiduoService service;
    private final CentroService centroService;
    private final UsuarioAutenticadoService authService;
    private final OwnershipService ownershipService;
    private final TrasladoRepository trasladoRepo;

    public ResiduoController(ResiduoService service, CentroService centroService,
                             UsuarioAutenticadoService authService, OwnershipService ownershipService,
                             TrasladoRepository trasladoRepo) {
        this.service = service;
        this.centroService = centroService;
        this.authService = authService;
        this.ownershipService = ownershipService;
        this.trasladoRepo = trasladoRepo;
    }

    @GetMapping
    public List<ResiduoDTO> listar() {
        Usuario usuario = authService.obtenerUsuarioActual();
        if (usuario == null) return List.of();
        return service.findAllForUsuario(usuario).stream().map(ResiduoDTO::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResiduoDTO> buscar(@PathVariable Long id) {
        Residuo r = service.findById(id);
        if (r == null) throw new RecursoNoEncontradoException("Residuo no encontrado: " + id);
        verificarAccesoResiduo(r);
        return ResponseEntity.ok(ResiduoDTO.from(r));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'PRODUCTOR')")
    public ResponseEntity<ResiduoDTO> crear(@RequestBody ResiduoInputDTO input) {
        Residuo r = mapInputToEntity(input);
        Residuo saved = service.save(r);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResiduoDTO.from(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResiduoDTO> editar(@PathVariable Long id, @RequestBody ResiduoInputDTO input) {
        Residuo existing = service.findById(id);
        if (existing == null) throw new RecursoNoEncontradoException("Residuo no encontrado: " + id);
        verificarAccesoResiduo(existing);

        Residuo r = mapInputToEntity(input);
        r.setId(id);
        return ResponseEntity.ok(ResiduoDTO.from(service.save(r)));
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

    private Residuo mapInputToEntity(ResiduoInputDTO input) {
        Centro centro = centroService.findById(input.centroId());
        if (centro == null) throw new RecursoNoEncontradoException("Centro no encontrado: " + input.centroId());

        Residuo r = new Residuo(input.cantidad(), input.unidad(), input.estado(), centro);
        r.setCodigoLER(input.codigoLER());
        r.setFechaEntradaAlmacen(input.fechaEntradaAlmacen());
        if (input.diasMaximoAlmacenamiento() != null) {
            r.setDiasMaximoAlmacenamiento(input.diasMaximoAlmacenamiento());
        }
        return r;
    }

    private void verificarAccesoResiduo(Residuo residuo) {
        Usuario usuario = authService.obtenerUsuarioActual();
        if (usuario == null) throw new AccesoDenegadoException("No autenticado");
        if (authService.esAdmin(usuario)) return;

        if (residuo.getCentro() == null
                || !ownershipService.canAccessCentro(usuario, residuo.getCentro().getId())) {
            throw new AccesoDenegadoException("No tiene acceso a este residuo");
        }
    }
}