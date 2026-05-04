package com.alejandrofernandez.ecoadmin.servicios;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alejandrofernandez.ecoadmin.excepciones.AccesoDenegadoException;
import com.alejandrofernandez.ecoadmin.excepciones.RecursoNoEncontradoException;
import com.alejandrofernandez.ecoadmin.modelo.Notificacion;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;
import com.alejandrofernandez.ecoadmin.repository.NotificacionRepository;
import com.alejandrofernandez.ecoadmin.repository.UsuarioRepository;

@Service
public class NotificacionService {

    private final NotificacionRepository repo;
    private final UsuarioRepository usuarioRepo;

    public NotificacionService(NotificacionRepository repo, UsuarioRepository usuarioRepo) {
        this.repo = repo;
        this.usuarioRepo = usuarioRepo;
    }

    public List<Notificacion> listar(Usuario dest) {
        return repo.findByDestinatarioOrderByFechaDesc(dest);
    }

    public List<Notificacion> noLeidas(Usuario dest) {
        return repo.findByDestinatarioAndLeidaFalseOrderByFechaDesc(dest);
    }

    public long contarNoLeidas(Usuario dest) {
        return repo.countByDestinatarioAndLeidaFalse(dest);
    }

    @Transactional
    public Notificacion marcarLeida(Long id, Usuario usuario) {
        Notificacion n = repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificacion no encontrada: " + id));
        if (!n.getDestinatario().getId().equals(usuario.getId())) {
            throw new AccesoDenegadoException("No es tu notificacion");
        }
        n.setLeida(true);
        return repo.save(n);
    }

    @Transactional
    public void marcarTodasLeidas(Usuario usuario) {
        repo.findByDestinatarioAndLeidaFalseOrderByFechaDesc(usuario)
                .forEach(notificacion -> notificacion.setLeida(true));
    }

    public void notificarAdmins(String titulo, String mensaje, String enlace) {
        List<Usuario> admins = usuarioRepo.findByRol(Rol.ADMIN);
        for (Usuario admin : admins) {
            repo.save(new Notificacion(admin, titulo, mensaje, enlace));
        }
    }

    public Notificacion crear(Notificacion notificacion) {
        return repo.save(notificacion);
    }
}
