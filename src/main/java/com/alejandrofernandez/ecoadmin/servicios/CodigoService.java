package com.alejandrofernandez.ecoadmin.servicios;

import java.time.Year;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alejandrofernandez.ecoadmin.modelo.SecuenciaCodigo;
import com.alejandrofernandez.ecoadmin.repository.SecuenciaCodigoRepository;

/**
 * Genera códigos alfanuméricos únicos y secuenciales por prefijo y año.
 * Formato: {PREFIJO}{AA}-{N6} — ejemplo: TRA26-000001, CEN26-000042.
 *
 * El método {@link #generar(String)} participa en la transacción activa
 * para evitar bloqueos de escritura en SQLite cuando se genera un código
 * durante otro guardado JPA.
 */
@Service
public class CodigoService {

    private static final int DIGITOS = 6;

    private final SecuenciaCodigoRepository secuenciaRepo;

    public CodigoService(SecuenciaCodigoRepository secuenciaRepo) {
        this.secuenciaRepo = secuenciaRepo;
    }

    /**
     * Genera el siguiente código para el prefijo dado.
     * Es atómico gracias al bloqueo pesimista sobre la fila de secuencia.
     *
     * @param prefijo p. ej. "TRA", "CEN", "RES", "REC", "USR", "DOC", "EMP"
     * @return código con formato TRA26-000001
     */
    @Transactional
    public synchronized String generar(String prefijo) {
        int anio = Year.now().getValue() % 100; // últimos dos dígitos: 26
        String clave = prefijo + "_" + anio;

        SecuenciaCodigo secuencia = secuenciaRepo.findByClaveConBloqueo(clave)
            .orElseGet(() -> new SecuenciaCodigo(clave));

        long numero = secuencia.incrementarYObtener();
        secuenciaRepo.save(secuencia);

        return String.format("%s%02d-%0" + DIGITOS + "d", prefijo, anio, numero);
    }
}
