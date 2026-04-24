package com.iesdoctorbalmis.spring.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Datos legales de la empresa que opera la plataforma EcoAdmin.
 * Singleton: solo se mantiene UN registro por instancia (id=1).
 * Estos datos se utilizan para pre-rellenar documentos legales (DI, NP, contratos).
 */
@Entity
@Table(name = "empresa")
public class Empresa {

    // Singleton: en la practica solo existe UN registro (el primero que cree el sistema).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String codigo;

    @PrePersist
    private void asignarCodigo() {
        if (this.codigo == null) {
            this.codigo = com.iesdoctorbalmis.spring.config.SpringContextHolder
                .getBean(com.iesdoctorbalmis.spring.servicios.CodigoService.class).generar("EMP");
        }
    }

    @Column(nullable = false)
    private String nombre = "";

    private String cif;
    private String nima;                    // Numero de Identificacion Medioambiental
    private String telefono;
    private String email;
    private String web;

    // Direccion fiscal (texto libre, no FK porque la empresa es singleton)
    private String direccion;
    private String ciudad;
    private String codigoPostal;
    private String provincia;
    private String pais;

    @Column(length = 500)
    private String autorizacionGestor;       // referencia o numero de autorizacion como gestor

    @Column(length = 500)
    private String autorizacionTransportista;

    @Column(length = 500)
    private String autorizacionProductor;

    private String logoUrl;

    @Column(length = 1000)
    private String observaciones;

    public Empresa() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCif() { return cif; }
    public void setCif(String cif) { this.cif = cif; }
    public String getNima() { return nima; }
    public void setNima(String nima) { this.nima = nima; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getWeb() { return web; }
    public void setWeb(String web) { this.web = web; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }
    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getAutorizacionGestor() { return autorizacionGestor; }
    public void setAutorizacionGestor(String autorizacionGestor) { this.autorizacionGestor = autorizacionGestor; }
    public String getAutorizacionTransportista() { return autorizacionTransportista; }
    public void setAutorizacionTransportista(String s) { this.autorizacionTransportista = s; }
    public String getAutorizacionProductor() { return autorizacionProductor; }
    public void setAutorizacionProductor(String s) { this.autorizacionProductor = s; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
