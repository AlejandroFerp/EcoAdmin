package com.iesdoctorbalmis.spring.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Relación M:N entre Ruta y transportistas (Usuario con rol TRANSPORTISTA).
 * Permite que una ruta tenga múltiples transportistas, cada uno con su propia
 * fórmula de tarifa. Si el transportista no define fórmula, se usa la de la ruta
 * como fallback.
 */
@Entity
@Table(
    name = "ruta_transportistas",
    uniqueConstraints = @UniqueConstraint(columnNames = {"ruta_id", "transportista_id"})
)
public class RutaTransportista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    @JsonIgnoreProperties({"fotoUrl", "notificacionesEmail", "telefono", "dni", "cargo"})
    @ManyToOne(optional = false)
    @JoinColumn(name = "transportista_id", nullable = false)
    private Usuario transportista;

    /** Fórmula específica de este transportista para esta ruta. Null → usa ruta.formulaTarifa. */
    @Column(columnDefinition = "TEXT")
    private String formulaTarifa;

    private String unidadTarifa;

    @Column(nullable = false)
    private boolean activo = true;

    public Long getId() { return id; }

    public Ruta getRuta() { return ruta; }
    public void setRuta(Ruta ruta) { this.ruta = ruta; }

    public Usuario getTransportista() { return transportista; }
    public void setTransportista(Usuario transportista) { this.transportista = transportista; }

    public String getFormulaTarifa() { return formulaTarifa; }
    public void setFormulaTarifa(String formulaTarifa) { this.formulaTarifa = formulaTarifa; }

    public String getUnidadTarifa() { return unidadTarifa; }
    public void setUnidadTarifa(String unidadTarifa) { this.unidadTarifa = unidadTarifa; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
