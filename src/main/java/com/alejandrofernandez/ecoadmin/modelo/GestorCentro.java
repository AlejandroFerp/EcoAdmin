package com.alejandrofernandez.ecoadmin.modelo;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.springframework.data.annotation.CreatedDate;

/**
 * Relación M:N entre gestores (Usuario con rol GESTOR) y centros.
 * Un centro puede tener múltiples gestores; un gestor puede gestionar múltiples centros.
 */
@Entity
@Table(name = "gestor_centros", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"gestor_id", "centro_id"})
})
public class GestorCentro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "gestor_id", nullable = false)
    private Usuario gestor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "centro_id", nullable = false)
    private Centro centro;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime fechaAsignacion;

    public GestorCentro() {}

    public GestorCentro(Usuario gestor, Centro centro) {
        this.gestor = gestor;
        this.centro = centro;
        this.fechaAsignacion = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public Usuario getGestor() { return gestor; }
    public void setGestor(Usuario gestor) { this.gestor = gestor; }

    public Centro getCentro() { return centro; }
    public void setCentro(Centro centro) { this.centro = centro; }

    public LocalDateTime getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }
}
