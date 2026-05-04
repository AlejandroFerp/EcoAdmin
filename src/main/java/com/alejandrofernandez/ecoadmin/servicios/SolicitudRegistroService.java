package com.alejandrofernandez.ecoadmin.servicios;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alejandrofernandez.ecoadmin.excepciones.RecursoNoEncontradoException;
import com.alejandrofernandez.ecoadmin.modelo.PerfilTransportista;
import com.alejandrofernandez.ecoadmin.modelo.SolicitudRegistro;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoSolicitud;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;
import com.alejandrofernandez.ecoadmin.repository.PerfilTransportistaRepository;
import com.alejandrofernandez.ecoadmin.repository.SolicitudRegistroRepository;
import com.alejandrofernandez.ecoadmin.repository.UsuarioRepository;

@Service
public class SolicitudRegistroService {

    private static final Logger log = LoggerFactory.getLogger(SolicitudRegistroService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final SolicitudRegistroRepository repo;
    private final UsuarioRepository usuarioRepo;
    private final PerfilTransportistaRepository ptRepo;
    private final PasswordEncoder passwordEncoder;
    private final NotificacionService notificacionService;

    public SolicitudRegistroService(SolicitudRegistroRepository repo,
                                     UsuarioRepository usuarioRepo,
                                     PerfilTransportistaRepository ptRepo,
                                     PasswordEncoder passwordEncoder,
                                     NotificacionService notificacionService) {
        this.repo = repo;
        this.usuarioRepo = usuarioRepo;
        this.ptRepo = ptRepo;
        this.passwordEncoder = passwordEncoder;
        this.notificacionService = notificacionService;
    }

    public List<SolicitudRegistro> listar() {
        return repo.findAllByOrderByFechaSolicitudDesc();
    }

    public List<SolicitudRegistro> listarPorEstado(EstadoSolicitud estado) {
        return repo.findByEstadoOrderByFechaSolicitudDesc(estado);
    }

    public SolicitudRegistro buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada: " + id));
    }

    @Transactional
    public SolicitudRegistro crear(SolicitudRegistro solicitud) {
        if (solicitud.getRolSolicitado() == null) {
            throw new IllegalArgumentException("El rol solicitado es obligatorio");
        }
        if (solicitud.getRolSolicitado() == Rol.ADMIN) {
            throw new IllegalArgumentException("No se puede solicitar el rol ADMIN");
        }
        if (solicitud.getNombre() == null || solicitud.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (solicitud.getEmail() == null || !EMAIL_PATTERN.matcher(solicitud.getEmail()).matches()) {
            throw new IllegalArgumentException("Email invalido");
        }
        if (usuarioRepo.findByEmail(solicitud.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }
        if (repo.existsByEmailAndEstado(solicitud.getEmail(), EstadoSolicitud.PENDIENTE)) {
            throw new IllegalArgumentException("Ya existe una solicitud pendiente con ese email");
        }

        SolicitudRegistro guardada = repo.save(solicitud);

        try {
            notificacionService.notificarAdmins(
                "Nueva solicitud de registro",
                solicitud.getNombre() + " solicita registrarse como " + solicitud.getRolSolicitado().name(),
                "/solicitudes"
            );
        } catch (Exception e) {
            log.warn("No se pudo notificar a admins sobre solicitud {}: {}", guardada.getId(), e.getMessage());
        }

        return guardada;
    }

    @Transactional
    public Usuario aprobar(Long id, String password, Usuario admin) {
        SolicitudRegistro sol = buscar(id);
        if (sol.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new IllegalArgumentException("La solicitud ya fue resuelta");
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombre(sol.getNombre());
        nuevo.setEmail(sol.getEmail());
        nuevo.setPassword(passwordEncoder.encode(password));
        nuevo.setRol(sol.getRolSolicitado());
        nuevo.setTelefono(sol.getTelefono());
        nuevo.setDni(sol.getDni());
        Usuario guardado = usuarioRepo.save(nuevo);

        if (sol.getRolSolicitado() == Rol.TRANSPORTISTA && sol.getMatricula() != null) {
            PerfilTransportista perfil = new PerfilTransportista();
            perfil.setUsuario(guardado);
            perfil.setMatricula(sol.getMatricula());
            ptRepo.save(perfil);
        }

        sol.setEstado(EstadoSolicitud.APROBADA);
        sol.setFechaResolucion(LocalDateTime.now());
        sol.setResueltoPor(admin);
        repo.save(sol);

        return guardado;
    }

    @Transactional
    public SolicitudRegistro rechazar(Long id, String motivo, Usuario admin) {
        SolicitudRegistro sol = buscar(id);
        if (sol.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new IllegalArgumentException("La solicitud ya fue resuelta");
        }

        sol.setEstado(EstadoSolicitud.RECHAZADA);
        sol.setMotivoRechazo(motivo);
        sol.setFechaResolucion(LocalDateTime.now());
        sol.setResueltoPor(admin);
        return repo.save(sol);
    }
}
