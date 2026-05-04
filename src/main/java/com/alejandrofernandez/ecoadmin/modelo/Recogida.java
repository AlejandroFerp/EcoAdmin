package com.alejandrofernandez.ecoadmin.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoRecogida;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "recogidas")
@EntityListeners(AuditingEntityListener.class)
public class Recogida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String codigo;

    @PrePersist
    private void asignarCodigo() {
        if (this.codigo == null) {
            this.codigo = com.alejandrofernandez.ecoadmin.config.SpringContextHolder
                .getBean(com.alejandrofernandez.ecoadmin.servicios.CodigoService.class).generar("REC");
        }
    }

    @ManyToOne
    @JoinColumn(name = "residuo_id")
    private Residuo residuo;

    @ManyToOne
    @JoinColumn(name = "centro_origen_id")
    private Centro centroOrigen;

    @ManyToOne
    @JoinColumn(name = "centro_destino_id")
    private Centro centroDestino;

    @ManyToOne
    @JoinColumn(name = "transportista_id")
    private Usuario transportista;

    @Column(nullable = false)
    private LocalDate fechaProgramada;

    private LocalDate fechaRealizada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRecogida estado = EstadoRecogida.PROGRAMADA;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    private LocalDateTime fechaModificacion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Residuo getResiduo() { return residuo; }
    public void setResiduo(Residuo residuo) { this.residuo = residuo; }
    public Centro getCentroOrigen() { return centroOrigen; }
    public void setCentroOrigen(Centro centroOrigen) { this.centroOrigen = centroOrigen; }
    public Centro getCentroDestino() { return centroDestino; }
    public void setCentroDestino(Centro centroDestino) { this.centroDestino = centroDestino; }
    public Usuario getTransportista() { return transportista; }
    public void setTransportista(Usuario transportista) { this.transportista = transportista; }
    public LocalDate getFechaProgramada() { return fechaProgramada; }
    public void setFechaProgramada(LocalDate fechaProgramada) { this.fechaProgramada = fechaProgramada; }
    public LocalDate getFechaRealizada() { return fechaRealizada; }
    public void setFechaRealizada(LocalDate fechaRealizada) { this.fechaRealizada = fechaRealizada; }
    public EstadoRecogida getEstado() { return estado; }
    public void setEstado(EstadoRecogida estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
}
