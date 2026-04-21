package com.iesdoctorbalmis.spring.modelo;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "adjuntos")
@EntityListeners(AuditingEntityListener.class)
public class Adjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String mimeType;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    @JsonIgnore
    private byte[] contenido;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime creadoEn;

    @ManyToOne
    @JoinColumn(name = "centro_id")
    @JsonIgnore
    private Centro centro;

    @ManyToOne
    @JoinColumn(name = "traslado_id")
    @JsonIgnore
    private Traslado traslado;

    public Adjunto() {}

    public Adjunto(String nombre, String mimeType, byte[] contenido) {
        this.nombre = nombre;
        this.mimeType = mimeType;
        this.contenido = contenido;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public byte[] getContenido() { return contenido; }
    public void setContenido(byte[] contenido) { this.contenido = contenido; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    public Centro getCentro() { return centro; }
    public void setCentro(Centro centro) { this.centro = centro; }
    public Traslado getTraslado() { return traslado; }
    public void setTraslado(Traslado traslado) { this.traslado = traslado; }
}
