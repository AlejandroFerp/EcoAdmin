package com.alejandrofernandez.ecoadmin.controladores;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alejandrofernandez.ecoadmin.excepciones.RecursoNoEncontradoException;
import com.alejandrofernandez.ecoadmin.modelo.Traslado;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoTraslado;
import com.alejandrofernandez.ecoadmin.servicios.QrService;
import com.alejandrofernandez.ecoadmin.servicios.TrasladoService;
import com.alejandrofernandez.ecoadmin.servicios.UsuarioAutenticadoService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "QR", description = "Generacion y lectura de codigos QR para traslados")
@Controller
public class QrController {

    private final QrService qrService;
    private final TrasladoService trasladoService;
    private final UsuarioAutenticadoService authService;

    public QrController(QrService qrService,
                        TrasladoService trasladoService,
                        UsuarioAutenticadoService authService) {
        this.qrService = qrService;
        this.trasladoService = trasladoService;
        this.authService = authService;
    }

    /**
     * Decodifica un QR desde imagen subida.
     */
    @PostMapping("/api/qr/leer")
    @ResponseBody
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

    /**
     * Punto de entrada al escanear un QR de traslado.
     * - PENDIENTE → EN_TRANSITO (salida/entrada registrada)
     * - EN_TRANSITO → COMPLETADO (entrega confirmada)
     * - COMPLETADO/ENTREGADO → ya registrado (sin cambio)
     * - Otro estado → error
     */
    @GetMapping("/qr/entrada")
    public String entradaQr(@RequestParam("id") Long trasladoId, Model model) {
        try {
            Traslado traslado = trasladoService.findById(trasladoId);
            EstadoTraslado estadoActual = traslado.getEstado();

            if (estadoActual == EstadoTraslado.COMPLETADO
                    || estadoActual == EstadoTraslado.ENTREGADO) {
                model.addAttribute("traslado", traslado);
                model.addAttribute("nuevoEstado", estadoActual.name());
                model.addAttribute("yaRegistrado", true);
                model.addAttribute("exito", true);
                return "qr-confirmacion";
            }

            var usuario = authService.obtenerUsuarioActual();

            if (estadoActual == EstadoTraslado.PENDIENTE) {
                Traslado actualizado = trasladoService.cambiarEstado(
                    trasladoId, EstadoTraslado.EN_TRANSITO,
                    "Entrada registrada via QR", usuario);
                model.addAttribute("traslado", actualizado);
                model.addAttribute("nuevoEstado", "EN_TRANSITO");
                model.addAttribute("yaRegistrado", false);
                model.addAttribute("exito", true);
                return "qr-confirmacion";
            }

            if (estadoActual == EstadoTraslado.EN_TRANSITO) {
                Traslado actualizado = trasladoService.cambiarEstado(
                    trasladoId, EstadoTraslado.COMPLETADO,
                    "Entrega confirmada via QR", usuario);
                model.addAttribute("traslado", actualizado);
                model.addAttribute("nuevoEstado", "COMPLETADO");
                model.addAttribute("yaRegistrado", false);
                model.addAttribute("exito", true);
                return "qr-confirmacion";
            }

            model.addAttribute("error", "El traslado está en estado " + estadoActual + " y no puede procesarse via QR.");
            model.addAttribute("trasladoId", trasladoId);
            return "qr-error";

        } catch (RecursoNoEncontradoException e) {
            model.addAttribute("error", "Traslado #" + trasladoId + " no encontrado.");
            model.addAttribute("trasladoId", trasladoId);
            return "qr-error";
        }
    }

    /**
     * Pagina con camara para escanear QR desde navegador (sin app movil).
     */
    @GetMapping("/qr/scanner")
    public String scannerPage() {
        return "qr-scanner";
    }
}

