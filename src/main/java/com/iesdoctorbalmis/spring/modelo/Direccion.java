package com.iesdoctorbalmis.spring.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "direcciones")
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String calle;
    private String ciudad;

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    private String provincia;

    @Column(length = 50)
    private String pais = "España";

    public Direccion() {}

    public Direccion(String calle, String ciudad, String codigoPostal, String provincia, String pais) {
        this.calle = calle;
        this.ciudad = ciudad;
        this.codigoPostal = codigoPostal;
        this.provincia = provincia;
        this.pais = pais;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }
    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    @Override
    public String toString() {
        return calle + ", " + codigoPostal + " " + ciudad + " (" + provincia + ")";
    }
}
