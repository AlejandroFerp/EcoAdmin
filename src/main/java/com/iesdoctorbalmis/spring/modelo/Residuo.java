package com.iesdoctorbalmis.spring.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "residuos")
public class Residuo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String codigo;

    @PrePersist
    private void asignarCodigo() {
        if (this.codigo == null) {
            this.codigo = com.iesdoctorbalmis.spring.config.SpringContextHolder
                .getBean(com.iesdoctorbalmis.spring.servicios.CodigoService.class).generar("RES");
        }
    }

    private double cantidad;
    private String unidad;
    private String estado;
    @Column(name = "codigo_ler")
    private String codigoLER;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "codigo_ler", referencedColumnName = "codigo", insertable = false, updatable = false)
    private ListaLer listaLer;

    private LocalDateTime fechaEntradaAlmacen;
    private LocalDateTime fechaSalidaAlmacen;

    @Column(columnDefinition = "INTEGER DEFAULT 180")
    private Integer diasMaximoAlmacenamiento = 180;

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
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setId(Long id) { this.id = id; }
    public Centro getCentro() { return centro; }
    public void setCentro(Centro centro) { this.centro = centro; }
    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getCodigoLER() {
        return listaLer != null ? listaLer.getCodigo() : codigoLER;
    }
    public void setCodigoLER(String codigoLER) {
        this.codigoLER = codigoLER;
        this.listaLer = null;
    }
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getDescripcion() {
        return listaLer != null ? listaLer.getDescripcion() : null;
    }
    public void setListaLer(ListaLer listaLer) {
        this.listaLer = listaLer;
        if (listaLer != null) {
            this.codigoLER = listaLer.getCodigo();
        }
    }
    public LocalDateTime getFechaEntradaAlmacen() { return fechaEntradaAlmacen; }
    public void setFechaEntradaAlmacen(LocalDateTime fechaEntradaAlmacen) { this.fechaEntradaAlmacen = fechaEntradaAlmacen; }
    public LocalDateTime getFechaSalidaAlmacen() { return fechaSalidaAlmacen; }
    public void setFechaSalidaAlmacen(LocalDateTime fechaSalidaAlmacen) { this.fechaSalidaAlmacen = fechaSalidaAlmacen; }
    public Integer getDiasMaximoAlmacenamiento() { return diasMaximoAlmacenamiento; }
    public void setDiasMaximoAlmacenamiento(Integer diasMaximoAlmacenamiento) { this.diasMaximoAlmacenamiento = diasMaximoAlmacenamiento; }

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
