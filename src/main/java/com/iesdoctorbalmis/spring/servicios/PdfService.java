package com.iesdoctorbalmis.spring.servicios;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class PdfService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final java.awt.Color COLOR_CABECERA = new java.awt.Color(26, 107, 60);
    private static final java.awt.Color COLOR_FILA_PAR  = new java.awt.Color(240, 248, 243);

    public byte[] generarCartaDePorte(Traslado traslado) {
        return generarDocumento("CARTA DE PORTE", traslado, true);
    }

    public byte[] generarNotificacionTraslado(Traslado traslado) {
        return generarDocumento("NOTIFICACIÓN DE TRASLADO", traslado, false);
    }

    public byte[] generarCertificadoRecepcion(Traslado traslado) {
        return generarDocumento("CERTIFICADO DE RECEPCIÓN", traslado, false);
    }

    // -------------------------------------------------------------------------
    // Implementación interna
    // -------------------------------------------------------------------------

    private byte[] generarDocumento(String tipo, Traslado traslado, boolean incluirTransportista) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50, 50, 60, 50);

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            escribirCabecera(doc, tipo);
            escribirDatosTraslado(doc, traslado, incluirTransportista);
            escribirDatosResiduo(doc, traslado);
            escribirPiePagina(doc, traslado);

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF para traslado " + traslado.getId(), e);
        } finally {
            doc.close();
        }

        return out.toByteArray();
    }

    private void escribirCabecera(Document doc, String tipo) {
        Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, COLOR_CABECERA);
        Font fuenteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 11,
                new java.awt.Color(100, 100, 100));

        Paragraph titulo = new Paragraph("EcoAdmin", fuenteTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(titulo);

        Paragraph subtitulo = new Paragraph("Gestión de Residuos Peligrosos", fuenteSubtitulo);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(4);
        doc.add(subtitulo);

        Font fuenteTipo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph tipoPar = new Paragraph(tipo, fuenteTipo);
        tipoPar.setAlignment(Element.ALIGN_CENTER);
        tipoPar.setSpacingBefore(10);
        tipoPar.setSpacingAfter(20);
        doc.add(tipoPar);
    }

    private void escribirDatosTraslado(Document doc, Traslado traslado,
                                       boolean incluirTransportista) {
        Font fuenteSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_CABECERA);
        doc.add(new Paragraph("Datos del traslado", fuenteSeccion));
        doc.add(new Paragraph(" "));

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(16);

        agregarFilaTabla(tabla, "ID de traslado", String.valueOf(traslado.getId()), true);
        agregarFilaTabla(tabla, "Estado", traslado.getEstado().name(), false);
        agregarFilaTabla(tabla, "Fecha creación",
                traslado.getFechaCreacion() != null
                        ? traslado.getFechaCreacion().format(FORMATO_FECHA) : "—", true);
        agregarFilaTabla(tabla, "Inicio transporte",
                traslado.getFechaInicioTransporte() != null
                        ? traslado.getFechaInicioTransporte().format(FORMATO_FECHA) : "—", false);
        agregarFilaTabla(tabla, "Fecha entrega",
                traslado.getFechaEntrega() != null
                        ? traslado.getFechaEntrega().format(FORMATO_FECHA) : "—", true);

        agregarFilaTabla(tabla, "Centro productor",
                traslado.getCentroProductor() != null ? traslado.getCentroProductor().getNombre() : "—", false);
        agregarFilaTabla(tabla, "Centro gestor",
                traslado.getCentroGestor() != null ? traslado.getCentroGestor().getNombre() : "—", true);

        if (incluirTransportista) {
            agregarFilaTabla(tabla, "Transportista",
                    traslado.getTransportista() != null ? traslado.getTransportista().getNombre() : "—", false);
        }

        if (traslado.getObservaciones() != null && !traslado.getObservaciones().isBlank()) {
            agregarFilaTabla(tabla, "Observaciones", traslado.getObservaciones(), true);
        }

        doc.add(tabla);
    }

    private void escribirDatosResiduo(Document doc, Traslado traslado) {
        if (traslado.getResiduo() == null) return;

        Font fuenteSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_CABECERA);
        doc.add(new Paragraph("Datos del residuo", fuenteSeccion));
        doc.add(new Paragraph(" "));

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(16);

        var residuo = traslado.getResiduo();
        agregarFilaTabla(tabla, "ID residuo", String.valueOf(residuo.getId()), true);
        agregarFilaTabla(tabla, "Código LER",
                residuo.getCodigoLER() != null ? residuo.getCodigoLER() : "—", false);
        agregarFilaTabla(tabla, "Cantidad",
                residuo.getCantidad() + " " + (residuo.getUnidad() != null ? residuo.getUnidad() : ""), true);
        agregarFilaTabla(tabla, "Estado", residuo.getEstado() != null ? residuo.getEstado() : "—", false);

        doc.add(tabla);
    }

    private void escribirPiePagina(Document doc, Traslado traslado) {
        Font fuentePie = FontFactory.getFont(FontFactory.HELVETICA, 9,
                new java.awt.Color(150, 150, 150));
        Paragraph pie = new Paragraph(
                "Documento generado automáticamente por EcoAdmin · Traslado #" + traslado.getId(),
                fuentePie);
        pie.setAlignment(Element.ALIGN_CENTER);
        pie.setSpacingBefore(30);
        doc.add(pie);
    }

    private void agregarFilaTabla(PdfPTable tabla, String etiqueta, String valor, boolean filaPar) {
        Font fuenteEtiqueta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font fuenteValor    = FontFactory.getFont(FontFactory.HELVETICA, 10);
        java.awt.Color fondo = filaPar ? COLOR_FILA_PAR : java.awt.Color.WHITE;

        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, fuenteEtiqueta));
        celdaEtiqueta.setBorder(Rectangle.BOTTOM);
        celdaEtiqueta.setBackgroundColor(fondo);
        celdaEtiqueta.setPadding(6);

        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, fuenteValor));
        celdaValor.setBorder(Rectangle.BOTTOM);
        celdaValor.setBackgroundColor(fondo);
        celdaValor.setPadding(6);

        tabla.addCell(celdaEtiqueta);
        tabla.addCell(celdaValor);
    }
}
