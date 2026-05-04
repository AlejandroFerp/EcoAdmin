package com.alejandrofernandez.ecoadmin.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Contador de secuencias por prefijo y año.
 * Garantiza que los códigos sean únicos y ordenados: {PREFIJO}{AA}-{N6}.
 * Ejemplo: TRA26-000001, CEN26-000001.
 */
@Entity
@Table(name = "secuencias_codigo")
public class SecuenciaCodigo {

    /** Clave compuesta natural: prefijo + año. Por simplicidad se usa un ID compuesto de texto. */
    @Id
    @Column(name = "clave", length = 10)
    private String clave; // "TRA_26", "CEN_26", etc.

    @Column(nullable = false)
    private long valorActual = 0;

    public SecuenciaCodigo() {}

    public SecuenciaCodigo(String clave) {
        this.clave = clave;
    }

    public String getClave() { return clave; }
    public long getValorActual() { return valorActual; }

    public long incrementarYObtener() {
        this.valorActual++;
        return this.valorActual;
    }
}
