package com.iesdoctorbalmis.spring.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.iesdoctorbalmis.spring.modelo.enums.EstadoDocumento;
import com.iesdoctorbalmis.spring.modelo.enums.TipoDocumento;

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
@Table(name = "documentos")
@EntityListeners(AuditingEntityListener.class)
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDocumento estado = EstadoDocumento.BORRADOR;

    private String numeroReferencia;

    @ManyToOne
    @JoinColumn(name = "traslado_id")
    private Traslado traslado;

    @ManyToOne
    @JoinColumn(name = "centro_id")
    private Centro centro;

    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private LocalDate fechaCierre;

    private String observaciones;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime creadoEn;

    public Documento() {}

    public Documento(TipoDocumento tipo, Traslado traslado, String numeroReferencia) {
        this.tipo = tipo;
        this.traslado = traslado;
        this.numeroReferencia = numeroReferencia;
        this.estado = EstadoDocumento.EMITIDO;
        this.fechaEmision = LocalDate.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoDocumento getTipo() { return tipo; }
    public void setTipo(TipoDocumento tipo) { this.tipo = tipo; }
    public EstadoDocumento getEstado() { return estado; }
    public void setEstado(EstadoDocumento estado) { this.estado = estado; }
    public String getNumeroReferencia() { return numeroReferencia; }
    public void setNumeroReferencia(String numeroReferencia) { this.numeroReferencia = numeroReferencia; }
    public Traslado getTraslado() { return traslado; }
    public void setTraslado(Traslado traslado) { this.traslado = traslado; }
    public Centro getCentro() { return centro; }
    public void setCentro(Centro centro) { this.centro = centro; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public LocalDate getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDate fechaCierre) { this.fechaCierre = fechaCierre; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
