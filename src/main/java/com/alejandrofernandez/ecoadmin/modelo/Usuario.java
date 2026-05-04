package com.alejandrofernandez.ecoadmin.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@EntityListeners(AuditingEntityListener.class)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String codigo;

    @PrePersist
    private void asignarCodigo() {
        if (this.codigo == null) {
            this.codigo = com.alejandrofernandez.ecoadmin.config.SpringContextHolder
                .getBean(com.alejandrofernandez.ecoadmin.servicios.CodigoService.class).generar("USR");
        }
    }

    private String nombre;

    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol = Rol.PRODUCTOR;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime fechaAlta;

    // Datos operativos para gestion de residuos (RD 553/2020 / NT)
    private String telefono;          // contacto operativo
    private String dni;               // DNI/NIF para firmar documentos
    private String cargo;             // p.ej. Tecnico ADR, Operario, Gerente

    @Column(columnDefinition = "TEXT")
    private String fotoUrl;           // URL avatar o data URL base64 (256x256 JPEG)

    private Boolean notificacionesEmail = Boolean.TRUE;

    public Usuario() {}

    public Usuario(String nombre, String email, String password, Rol rol) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public LocalDateTime getFechaAlta() { return fechaAlta; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    public boolean isNotificacionesEmail() { return notificacionesEmail == null || notificacionesEmail; }
    public void setNotificacionesEmail(boolean notificacionesEmail) { this.notificacionesEmail = notificacionesEmail; }
}
