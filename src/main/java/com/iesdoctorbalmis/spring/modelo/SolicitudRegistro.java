package com.iesdoctorbalmis.spring.modelo;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoSolicitud;
import com.iesdoctorbalmis.spring.modelo.enums.Rol;

@Entity
@Table(name = "solicitudes_registro")
public class SolicitudRegistro {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String nombre;
    @Column(nullable = false) private String email;
    private String telefono;
    private String dni;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Rol rolSolicitado;

    private String empresa;
    private String nima;
    private String autorizacionGestor;
    private String matricula;
    private String certificadoAdr;
    private String centroPrincipal;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Column(length = 1000) private String motivoRechazo;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaResolucion;
    @ManyToOne private Usuario resueltoPor;

    @PrePersist
    private void prePersist() {
        if (fechaSolicitud == null) fechaSolicitud = LocalDateTime.now();
    }

    public SolicitudRegistro() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public Rol getRolSolicitado() { return rolSolicitado; }
    public void setRolSolicitado(Rol r) { this.rolSolicitado = r; }
    public String getEmpresa() { return empresa; }
    public void setEmpresa(String e) { this.empresa = e; }
    public String getNima() { return nima; }
    public void setNima(String n) { this.nima = n; }
    public String getAutorizacionGestor() { return autorizacionGestor; }
    public void setAutorizacionGestor(String a) { this.autorizacionGestor = a; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String m) { this.matricula = m; }
    public String getCertificadoAdr() { return certificadoAdr; }
    public void setCertificadoAdr(String c) { this.certificadoAdr = c; }
    public String getCentroPrincipal() { return centroPrincipal; }
    public void setCentroPrincipal(String c) { this.centroPrincipal = c; }
    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud e) { this.estado = e; }
    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String m) { this.motivoRechazo = m; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime f) { this.fechaSolicitud = f; }
    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime f) { this.fechaResolucion = f; }
    public Usuario getResueltoPor() { return resueltoPor; }
    public void setResueltoPor(Usuario u) { this.resueltoPor = u; }
}
