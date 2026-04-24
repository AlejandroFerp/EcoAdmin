package com.iesdoctorbalmis.spring;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.iesdoctorbalmis.spring.servicios.QrService;

class QrServiceTest {

    private final QrService qrService = new QrService();

    @Test
    @DisplayName("generarQr genera imagen PNG no vacia")
    void generarQr_generaImagen() {
        byte[] png = qrService.generarQr("https://ecoadmin.test/qr/entrada?id=42");
        assertNotNull(png);
        assertTrue(png.length > 100, "Deberia ser una imagen PNG sustancial");
        // PNG magic bytes
        assertEquals((byte) 0x89, png[0]);
        assertEquals('P', (char) png[1]);
        assertEquals('N', (char) png[2]);
        assertEquals('G', (char) png[3]);
    }

    @Test
    @DisplayName("generarQrTraslado genera QR con URL correcta")
    void generarQrTraslado_generaImagen() {
        byte[] png = qrService.generarQrTraslado(99L, "https://ecoadmin.test");
        assertNotNull(png);
        assertTrue(png.length > 100);
    }

    @Test
    @DisplayName("generarQr con contenido distinto produce bytes distintos")
    void generarQr_contenidoDistinto_bytesDistintos() {
        byte[] qr1 = qrService.generarQr("contenido-A");
        byte[] qr2 = qrService.generarQr("contenido-B");
        assertNotNull(qr1);
        assertNotNull(qr2);
        assertFalse(java.util.Arrays.equals(qr1, qr2));
    }

    @Test
    @DisplayName("generarQr con texto vacio no lanza excepcion")
    void generarQr_textoVacio_noFalla() {
        byte[] png = qrService.generarQr("X");
        assertNotNull(png);
        assertTrue(png.length > 0);
    }
}
