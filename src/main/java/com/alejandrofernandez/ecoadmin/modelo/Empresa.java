package com.alejandrofernandez.ecoadmin.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
            this.codigo = com.alejandrofernandez.ecoadmin.config.SpringContextHolder
                .getBean(com.alejandrofernandez.ecoadmin.servicios.CodigoService.class).generar("EMP");
        }
    }

    @Column(nullable = false)
    private String nombre = "";

    private String cif;
    private String nima;                    // Numero de Identificacion Medioambiental
    private String telefono;
    private String email;
    private String web;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "direccion_id")
    private Direccion direccionFiscal;

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
    public String getDireccion() { return direccionFiscal != null ? direccionFiscal.getCalle() : null; }
    public void setDireccion(String direccion) {
        if (direccionFiscal == null && !hasText(direccion)) return;
        asegurarDireccionFiscal().setCalle(normalizar(direccion));
    }
    public String getCiudad() { return direccionFiscal != null ? direccionFiscal.getCiudad() : null; }
    public void setCiudad(String ciudad) {
        if (direccionFiscal == null && !hasText(ciudad)) return;
        asegurarDireccionFiscal().setCiudad(normalizar(ciudad));
    }
    public String getCodigoPostal() { return direccionFiscal != null ? direccionFiscal.getCodigoPostal() : null; }
    public void setCodigoPostal(String codigoPostal) {
        if (direccionFiscal == null && !hasText(codigoPostal)) return;
        asegurarDireccionFiscal().setCodigoPostal(normalizar(codigoPostal));
    }
    public String getProvincia() { return direccionFiscal != null ? direccionFiscal.getProvincia() : null; }
    public void setProvincia(String provincia) {
        if (direccionFiscal == null && !hasText(provincia)) return;
        asegurarDireccionFiscal().setProvincia(normalizar(provincia));
    }
    public String getPais() { return direccionFiscal != null ? direccionFiscal.getPais() : null; }
    public void setPais(String pais) {
        if (direccionFiscal == null && !hasText(pais)) return;
        asegurarDireccionFiscal().setPais(normalizar(pais));
    }
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

    @JsonIgnore
    public Direccion getDireccionFiscal() { return direccionFiscal; }

    @JsonIgnore
    public void setDireccionFiscal(Direccion direccionFiscal) { this.direccionFiscal = direccionFiscal; }

    public void normalizarDireccionFiscal() {
        if (direccionFiscal == null) {
            return;
        }

        direccionFiscal.setCalle(normalizar(direccionFiscal.getCalle()));
        direccionFiscal.setCiudad(normalizar(direccionFiscal.getCiudad()));
        direccionFiscal.setCodigoPostal(normalizar(direccionFiscal.getCodigoPostal()));
        direccionFiscal.setProvincia(normalizar(direccionFiscal.getProvincia()));
        direccionFiscal.setPais(normalizar(direccionFiscal.getPais()));

        if (!hasText(direccionFiscal.getNombre()) && hasText(nombre)) {
            direccionFiscal.setNombre("Direccion fiscal " + nombre.trim());
        }

        if (!hasText(direccionFiscal.getCalle())
                && !hasText(direccionFiscal.getCiudad())
                && !hasText(direccionFiscal.getCodigoPostal())
                && !hasText(direccionFiscal.getProvincia())
                && !hasText(direccionFiscal.getPais())) {
            direccionFiscal = null;
        }
    }

    private Direccion asegurarDireccionFiscal() {
        if (direccionFiscal == null) {
            direccionFiscal = new Direccion();
        }
        return direccionFiscal;
    }

    private static String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        String texto = valor.trim();
        return texto.isEmpty() ? null : texto;
    }

    private static boolean hasText(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }
}
