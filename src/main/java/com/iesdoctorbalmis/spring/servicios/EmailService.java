package com.iesdoctorbalmis.spring.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.iesdoctorbalmis.spring.modelo.Traslado;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${ecoadmin.mail.from}")
    private String remitente;

    @Async
    public void notificarNuevoTraslado(Traslado traslado) {
        if (traslado.getTransportista() == null
                || traslado.getTransportista().getEmail() == null) return;

        String destino = traslado.getTransportista().getEmail();
        String asunto  = "EcoAdmin — Nuevo traslado asignado #" + traslado.getId();
        String cuerpo  = construirCuerpoTraslado(traslado, "Se le ha asignado un nuevo traslado.");

        enviar(destino, asunto, cuerpo);
    }

    @Async
    public void notificarCambioEstado(Traslado traslado) {
        if (traslado.getTransportista() == null
                || traslado.getTransportista().getEmail() == null) return;

        String destino = traslado.getTransportista().getEmail();
        String asunto  = "EcoAdmin — Traslado #" + traslado.getId()
                + " → " + traslado.getEstado().name();
        String cuerpo  = construirCuerpoTraslado(traslado,
                "El estado del traslado ha cambiado a: " + traslado.getEstado().name());

        enviar(destino, asunto, cuerpo);
    }

    @Async
    public void enviarCertificado(Traslado traslado, String emailDestino) {
        String asunto = "EcoAdmin — Certificado de recepción traslado #" + traslado.getId();
        String cuerpo = construirCuerpoTraslado(traslado,
                "Adjunto encontrará el certificado de recepción del traslado.");
        enviar(emailDestino, asunto, cuerpo);
    }

    // -------------------------------------------------------------------------

    private void enviar(String destino, String asunto, String cuerpo) {
        if (mailSender == null) {
            System.out.println("[EmailService] Sin JavaMailSender — email no enviado a " + destino);
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
            // Log pero no propaga — el email es un servicio auxiliar que no debe
            // bloquear la operación principal si falla
            System.err.println("[EmailService] Error al enviar a " + destino + ": " + e.getMessage());
        }
    }

    private String construirCuerpoTraslado(Traslado traslado, String mensaje) {
        return mensaje + "\n\n"
             + "Traslado #" + traslado.getId() + "\n"
             + "Estado: " + traslado.getEstado().name() + "\n"
             + "Centro productor: "
             + (traslado.getCentroProductor() != null ? traslado.getCentroProductor().getNombre() : "—") + "\n"
             + "Centro gestor: "
             + (traslado.getCentroGestor() != null ? traslado.getCentroGestor().getNombre() : "—") + "\n\n"
             + "Acceda a EcoAdmin para más información.";
    }
}
