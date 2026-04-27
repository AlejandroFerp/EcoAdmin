package com.iesdoctorbalmis.spring.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoRuta;

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
@Table(name = "rutas")
@EntityListeners(AuditingEntityListener.class)
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "origen_direccion_id")
    private Direccion origen;

    @ManyToOne
    @JoinColumn(name = "destino_direccion_id")
    private Direccion destino;

    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRuta estado = EstadoRuta.PLANIFICADA;

    private Double distanciaKm;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    private String formulaTarifa;
    private String unidadTarifa;

    /** Transportistas asignados a esta ruta con sus tarifas propias. Servido via endpoint dedicado. */
    @JsonIgnore
    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RutaTransportista> asignaciones = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Direccion getOrigen() { return origen; }
    public void setOrigen(Direccion o) { this.origen = o; }
    public Direccion getDestino() { return destino; }
    public void setDestino(Direccion d) { this.destino = d; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public EstadoRuta getEstado() { return estado; }
    public void setEstado(EstadoRuta estado) { this.estado = estado != null ? estado : EstadoRuta.PLANIFICADA; }
    public Double getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(Double distanciaKm) { this.distanciaKm = distanciaKm; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getFormulaTarifa() { return formulaTarifa; }
    public void setFormulaTarifa(String f) { this.formulaTarifa = f; }
    public String getUnidadTarifa() { return unidadTarifa; }
    public void setUnidadTarifa(String u) { this.unidadTarifa = u; }
    public List<RutaTransportista> getAsignaciones() { return asignaciones; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}