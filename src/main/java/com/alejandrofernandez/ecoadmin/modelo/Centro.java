package com.alejandrofernandez.ecoadmin.modelo;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "centros")
public class Centro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String codigo;

    @PrePersist
    private void asignarCodigo() {
        if (this.codigo == null) {
            this.codigo = com.alejandrofernandez.ecoadmin.config.SpringContextHolder
                .getBean(com.alejandrofernandez.ecoadmin.servicios.CodigoService.class).generar("CEN");
        }
    }

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private String nombre;
    private String tipo;

    @ManyToOne
    @JoinColumn(name = "direccion_id")
    private Direccion direccion;

    private String nima;
    private String telefono;
    private String email;
    private String nombreContacto;
    private String detalleEnvio;

    @OneToMany(mappedBy = "centro")
    @JsonIgnore
    private List<Residuo> residuos;

    public Centro() {}

    public Centro(Usuario usuario, String nombre, String tipo, Direccion direccion) {
        this.usuario = usuario;
        this.nombre = nombre;
        this.tipo = tipo;
        this.direccion = direccion;
    }

    public Centro(String nombre, String tipo, Direccion direccion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.direccion = direccion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Direccion getDireccion() { return direccion; }
    public void setDireccion(Direccion direccion) { this.direccion = direccion; }
    public String getNima() { return nima; }
    public void setNima(String nima) { this.nima = nima; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNombreContacto() { return nombreContacto; }
    public void setNombreContacto(String nombreContacto) { this.nombreContacto = nombreContacto; }
    public String getDetalleEnvio() { return detalleEnvio; }
    public void setDetalleEnvio(String detalleEnvio) { this.detalleEnvio = detalleEnvio; }
    public List<Residuo> getResiduos() { return residuos; }

    @Override
    public String toString() {
        return "Centro [id=" + id + ", nombre=" + nombre + ", tipo=" + tipo + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre, tipo);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Centro other = (Centro) obj;
        return Objects.equals(id, other.id)
            && Objects.equals(nombre, other.nombre)
            && Objects.equals(tipo, other.tipo);
    }
}
