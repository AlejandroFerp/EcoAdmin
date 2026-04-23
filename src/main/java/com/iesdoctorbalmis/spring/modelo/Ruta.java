package com.iesdoctorbalmis.spring.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoRuta;

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
@Table(name = "rutas")
@EntityListeners(AuditingEntityListener.class)
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @JsonIgnoreProperties({"fotoUrl", "notificacionesEmail", "telefono", "dni", "cargo"})
    @ManyToOne
    @JoinColumn(name = "transportista_id")
    private Usuario transportista;

    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRuta estado = EstadoRuta.PLANIFICADA;

    private String origenDireccion;
    private String destinoDireccion;
    private Double distanciaKm;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Usuario getTransportista() { return transportista; }
    public void setTransportista(Usuario transportista) { this.transportista = transportista; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public EstadoRuta getEstado() { return estado; }
    public void setEstado(EstadoRuta estado) { this.estado = estado != null ? estado : EstadoRuta.PLANIFICADA; }
    public String getOrigenDireccion() { return origenDireccion; }
    public void setOrigenDireccion(String o) { this.origenDireccion = o; }
    public String getDestinoDireccion() { return destinoDireccion; }
    public void setDestinoDireccion(String d) { this.destinoDireccion = d; }
    public Double getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(Double distanciaKm) { this.distanciaKm = distanciaKm; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}