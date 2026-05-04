package com.alejandrofernandez.ecoadmin.servicios;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alejandrofernandez.ecoadmin.excepciones.RecursoNoEncontradoException;
import com.alejandrofernandez.ecoadmin.modelo.PerfilTransportista;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;
import com.alejandrofernandez.ecoadmin.repository.PerfilTransportistaRepository;
import com.alejandrofernandez.ecoadmin.repository.UsuarioRepository;

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
