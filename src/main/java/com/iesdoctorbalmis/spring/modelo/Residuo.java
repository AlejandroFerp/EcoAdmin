package com.iesdoctorbalmis.spring.modelo;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "residuos")
public class Residuo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double cantidad;
    private String unidad;
    private String estado;
    private String codigoLER;
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "centro_id")
    private Centro centro;

    public Residuo() {}

    public Residuo(double cantidad, String unidad, String estado, Centro centro) {
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.estado = estado;
        this.centro = centro;
    }

    public Residuo(double cantidad, String unidad, String estado) {
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Centro getCentro() { return centro; }
    public void setCentro(Centro centro) { this.centro = centro; }
    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getCodigoLER() { return codigoLER; }
    public void setCodigoLER(String codigoLER) { this.codigoLER = codigoLER; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() {
        return "Residuo [id=" + id + ", cantidad=" + cantidad + ", unidad=" + unidad + ", estado=" + estado + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cantidad, unidad, estado);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Residuo other = (Residuo) obj;
        return Double.compare(other.cantidad, cantidad) == 0
            && Objects.equals(id, other.id)
            && Objects.equals(unidad, other.unidad)
            && Objects.equals(estado, other.estado);
    }
}
