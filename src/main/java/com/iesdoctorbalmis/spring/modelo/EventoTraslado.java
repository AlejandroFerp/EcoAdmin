package com.iesdoctorbalmis.spring.modelo;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "eventos_traslado")
@EntityListeners(AuditingEntityListener.class)
public class EventoTraslado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "traslado_id", nullable = false)
    private Traslado traslado;

    @Enumerated(EnumType.STRING)
    private EstadoTraslado estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTraslado estadoNuevo;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime fecha;

    private String comentario;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public EventoTraslado() {}

    public EventoTraslado(Traslado traslado, EstadoTraslado estadoAnterior,
                          EstadoTraslado estadoNuevo, String comentario, Usuario usuario) {
        this.traslado = traslado;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.comentario = comentario;
        this.usuario = usuario;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Traslado getTraslado() { return traslado; }
    public void setTraslado(Traslado traslado) { this.traslado = traslado; }
    public EstadoTraslado getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(EstadoTraslado estadoAnterior) { this.estadoAnterior = estadoAnterior; }
    public EstadoTraslado getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(EstadoTraslado estadoNuevo) { this.estadoNuevo = estadoNuevo; }
    public LocalDateTime getFecha() { return fecha; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
