package com.iesdoctorbalmis.spring.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "perfil_transportista")
public class PerfilTransportista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", unique = true, nullable = false)
    private Usuario usuario;

    private String matricula;

    /** Expresion matematica con variables w (peso kg) y L (distancia km). Ej: "w * 0.5 + L * 0.1" */
    @Column(name = "formula_tarifa")
    private String formulaTarifa;

    /** Descripcion de la unidad resultante. Ej: "EUR/operacion" */
    @Column(name = "unidad_tarifa")
    private String unidadTarifa;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    public PerfilTransportista() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getFormulaTarifa() { return formulaTarifa; }
    public void setFormulaTarifa(String formulaTarifa) { this.formulaTarifa = formulaTarifa; }
    public String getUnidadTarifa() { return unidadTarifa; }
    public void setUnidadTarifa(String unidadTarifa) { this.unidadTarifa = unidadTarifa; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
