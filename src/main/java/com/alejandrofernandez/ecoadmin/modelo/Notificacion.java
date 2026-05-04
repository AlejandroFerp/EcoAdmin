package com.alejandrofernandez.ecoadmin.modelo;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) private Usuario destinatario;
    @Column(nullable = false) private String titulo;
    @Column(length = 1000) private String mensaje;
    private String enlace;
    @Column(nullable = false) private Boolean leida = false;
    private LocalDateTime fecha;

    @PrePersist
    private void prePersist() {
        if (fecha == null) fecha = LocalDateTime.now();
        if (leida == null) leida = false;
    }

    public Notificacion() {}
    public Notificacion(Usuario dest, String titulo, String msg, String enlace) {
        this.destinatario = dest; this.titulo = titulo; this.mensaje = msg; this.enlace = enlace;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getDestinatario() { return destinatario; }
    public void setDestinatario(Usuario d) { this.destinatario = d; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String t) { this.titulo = t; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String m) { this.mensaje = m; }
    public String getEnlace() { return enlace; }
    public void setEnlace(String e) { this.enlace = e; }
    public Boolean getLeida() { return leida; }
    public void setLeida(Boolean l) { this.leida = l; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime f) { this.fecha = f; }
}
