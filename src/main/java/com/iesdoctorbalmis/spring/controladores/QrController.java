package com.iesdoctorbalmis.spring.controladores;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.iesdoctorbalmis.spring.servicios.QrService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "QR", description = "Generacion y lectura de codigos QR para traslados")
@RestController
@RequestMapping("/api/qr")
public class QrController {

    @Autowired
    private QrService qrService;

    /**
     * Decodes a QR code from an uploaded image file.
     * Returns the text content encoded in the QR.
     */
    @PostMapping("/leer")
    public ResponseEntity<?> leerQr(@RequestParam("archivo") MultipartFile archivo) {
        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No se proporcionó ningún archivo."));
        }
        try {
            String contenido = qrService.leerQr(archivo);
            return ResponseEntity.ok(Map.of("contenido", contenido));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
