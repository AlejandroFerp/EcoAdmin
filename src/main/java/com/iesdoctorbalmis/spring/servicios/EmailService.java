package com.iesdoctorbalmis.spring.servicios;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.iesdoctorbalmis.spring.modelo.Traslado;

import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${ecoadmin.mail.from}")
    private String remitente;

    @PostConstruct
    void avisarSiSinMailSender() {
        // Fail fast: avisar claramente al arranque si el mail no esta configurado,
        // en lugar de fallar silenciosamente solo cuando alguien intenta enviar.
        if (mailSender == null) {
            log.warn("[EmailService] JavaMailSender no configurado: las notificaciones por email "
                    + "estaran deshabilitadas. Defina las variables MAIL_HOST/MAIL_USERNAME/MAIL_PASSWORD.");
        }
    }

    @Async
    public void notificarNuevoTraslado(Traslado traslado) {
        if (!tieneEmailTransportista(traslado)) return;

        String destino = traslado.getTransportista().getEmail();
        String asunto  = "EcoAdmin - Nuevo traslado asignado #" + traslado.getId();
        String cuerpo  = construirCuerpoTraslado(traslado, "Se le ha asignado un nuevo traslado.");

        enviarTexto(destino, asunto, cuerpo);
    }

    @Async
    public void notificarCambioEstado(Traslado traslado) {
        if (!tieneEmailTransportista(traslado)) return;

        String destino = traslado.getTransportista().getEmail();
        String asunto  = "EcoAdmin - Traslado #" + traslado.getId()
                + " -> " + traslado.getEstado().name();
        String cuerpo  = construirCuerpoTraslado(traslado,
                "El estado del traslado ha cambiado a: " + traslado.getEstado().name());

        enviarTexto(destino, asunto, cuerpo);
    }

    /**
     * Envia el certificado de recepcion como PDF adjunto al destinatario indicado.
     * Si {@code pdfCertificado} es null o esta vacio, se envia solo el cuerpo de texto
     * (degradacion controlada para no perder la notificacion).
     */
    @Async
    public void enviarCertificado(Traslado traslado, String emailDestino, byte[] pdfCertificado) {
        String asunto = "EcoAdmin - Certificado de recepcion traslado #" + traslado.getId();
        String cuerpo = construirCuerpoTraslado(traslado,
                "Adjunto encontrara el certificado de recepcion del traslado.");
        String nombreFichero = "certificado-traslado-" + traslado.getId() + ".pdf";

        if (pdfCertificado == null || pdfCertificado.length == 0) {
            log.warn("[EmailService] Certificado vacio para traslado {}, se envia solo texto",
                    traslado.getId());
            enviarTexto(emailDestino, asunto, cuerpo);
            return;
        }
        enviarConAdjunto(emailDestino, asunto, cuerpo, nombreFichero, pdfCertificado);
    }

    // -------------------------------------------------------------------------

    private boolean tieneEmailTransportista(Traslado traslado) {
        return traslado.getTransportista() != null
                && traslado.getTransportista().getEmail() != null
                && !traslado.getTransportista().getEmail().isBlank();
    }

    private void enviarTexto(String destino, String asunto, String cuerpo) {
        if (mailSender == null) {
            log.debug("[EmailService] Sin mailSender, omitiendo email a {}", destino);
            return;
        }
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destino);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            mailSender.send(mensaje);
        } catch (Exception e) {
            // El email es auxiliar: no debe romper la operacion principal si falla
            log.error("[EmailService] Error enviando email a {}: {}", destino, e.getMessage(), e);
        }
    }

    private void enviarConAdjunto(String destino, String asunto, String cuerpo,
                                  String nombreFichero, byte[] adjunto) {
        if (mailSender == null) {
            log.debug("[EmailService] Sin mailSender, omitiendo email con adjunto a {}", destino);
            return;
        }
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(destino);
            helper.setSubject(asunto);
            helper.setText(cuerpo);
            helper.addAttachment(nombreFichero, new ByteArrayResource(adjunto));
            mailSender.send(mime);
        } catch (Exception e) {
            log.error("[EmailService] Error enviando email con adjunto a {}: {}",
                    destino, e.getMessage(), e);
        }
    }

    private String construirCuerpoTraslado(Traslado traslado, String mensaje) {
        return mensaje + "\n\n"
             + "Traslado #" + traslado.getId() + "\n"
             + "Estado: " + traslado.getEstado().name() + "\n"
             + "Centro productor: "
             + (traslado.getCentroProductor() != null ? traslado.getCentroProductor().getNombre() : "-") + "\n"
             + "Centro gestor: "
             + (traslado.getCentroGestor() != null ? traslado.getCentroGestor().getNombre() : "-") + "\n\n"
             + "Acceda a EcoAdmin para mas informacion.";
    }
}
