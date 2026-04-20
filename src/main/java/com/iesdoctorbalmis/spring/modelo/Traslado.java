package com.iesdoctorbalmis.spring.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "traslados")
@EntityListeners(AuditingEntityListener.class)
public class Traslado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "centro_productor_id", nullable = false)
    private Centro centroProductor;

    @ManyToOne
    @JoinColumn(name = "centro_gestor_id", nullable = false)
    private Centro centroGestor;

    @ManyToOne
    @JoinColumn(name = "residuo_id", nullable = false)
    private Residuo residuo;

    @ManyToOne
    @JoinColumn(name = "transportista_id")
    private Usuario transportista;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTraslado estado = EstadoTraslado.PENDIENTE;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaInicioTransporte;
    private LocalDateTime fechaEntrega;

    private String observaciones;

    @OneToMany(mappedBy = "traslado", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<EventoTraslado> historial = new ArrayList<>();

    public Traslado() {}

    public Traslado(Centro centroProductor, Centro centroGestor, Residuo residuo, Usuario transportista) {
        this.centroProductor = centroProductor;
        this.centroGestor = centroGestor;
        this.residuo = residuo;
        this.transportista = transportista;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Centro getCentroProductor() { return centroProductor; }
    public void setCentroProductor(Centro centroProductor) { this.centroProductor = centroProductor; }
    public Centro getCentroGestor() { return centroGestor; }
    public void setCentroGestor(Centro centroGestor) { this.centroGestor = centroGestor; }
    public Residuo getResiduo() { return residuo; }
    public void setResiduo(Residuo residuo) { this.residuo = residuo; }
    public Usuario getTransportista() { return transportista; }
    public void setTransportista(Usuario transportista) { this.transportista = transportista; }
    public EstadoTraslado getEstado() { return estado; }
    public void setEstado(EstadoTraslado estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaInicioTransporte() { return fechaInicioTransporte; }
    public void setFechaInicioTransporte(LocalDateTime fecha) { this.fechaInicioTransporte = fecha; }
    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public List<EventoTraslado> getHistorial() { return historial; }
}
