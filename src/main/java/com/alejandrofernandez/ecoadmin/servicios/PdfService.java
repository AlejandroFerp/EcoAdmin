package com.alejandrofernandez.ecoadmin.servicios;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alejandrofernandez.ecoadmin.modelo.Empresa;
import com.alejandrofernandez.ecoadmin.modelo.Traslado;
import com.alejandrofernandez.ecoadmin.repository.EmpresaRepository;
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

    private final EmpresaRepository empresaRepo;

    public PdfService(EmpresaRepository empresaRepo) {
        this.empresaRepo = empresaRepo;
    }

    private Empresa obtenerEmpresa() {
        return empresaRepo.findAll().stream().findFirst().orElse(null);
    }

    private String nombreEmpresa() {
        Empresa e = obtenerEmpresa();
        return (e != null && e.getNombre() != null && !e.getNombre().isBlank()) ? e.getNombre() : "EcoAdmin";
    }

    public byte[] generarCartaDePorte(Traslado traslado) {
        return generarDocumento("CARTA DE PORTE", traslado, true);
    }

    public byte[] generarNotificacionTraslado(Traslado traslado) {
        return generarDocumento("NOTIFICACIÓN DE TRASLADO", traslado, false);
    }

    public byte[] generarCertificadoRecepcion(Traslado traslado) {
        return generarDocumento("CERTIFICADO DE RECEPCIÓN", traslado, false);
    }

    public byte[] generarFichaAceptacion(Traslado traslado) {
        return generarDocumento("FICHA DE ACEPTACIÓN", traslado, false);
    }

    public byte[] generarHojaSeguimiento(Traslado traslado) {
        return generarDocumento("HOJA DE SEGUIMIENTO", traslado, true);
    }

    public byte[] generarInformeDocumento(Traslado traslado) {
        return generarDocumento("INFORME DEL TRASLADO", traslado, false);
    }

    public byte[] generarDocumentoContrato(Traslado traslado) {
        return generarDocumento("CONTRATO / ACUERDO", traslado, false);
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
        Empresa empresa = obtenerEmpresa();
        String titulo_text = (empresa != null && empresa.getNombre() != null && !empresa.getNombre().isBlank())
                ? empresa.getNombre() : "EcoAdmin";

        Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, COLOR_CABECERA);
        Font fuenteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 11,
                new java.awt.Color(100, 100, 100));

        Paragraph titulo = new Paragraph(titulo_text, fuenteTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(titulo);

        // CIF / NIMA bajo el titulo si disponible
        if (empresa != null) {
            StringBuilder datos = new StringBuilder("Gestión de Residuos Peligrosos");
            if (empresa.getCif() != null && !empresa.getCif().isBlank())
                datos.append("  ·  CIF: ").append(empresa.getCif());
            if (empresa.getNima() != null && !empresa.getNima().isBlank())
                datos.append("  ·  NIMA: ").append(empresa.getNima());
            Paragraph subtitulo = new Paragraph(datos.toString(), fuenteSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(4);
            doc.add(subtitulo);
        } else {
            Paragraph subtitulo = new Paragraph("Gestión de Residuos Peligrosos", fuenteSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(4);
            doc.add(subtitulo);
        }

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
                "Documento generado automáticamente por " + nombreEmpresa() + " · Traslado #" + traslado.getId(),
                fuentePie);
        pie.setAlignment(Element.ALIGN_CENTER);
        pie.setSpacingBefore(30);
        doc.add(pie);
    }

    // ─── Informe Final de Gestion (PDF) ────────────────────────────────────

    public byte[] generarInformeFinalGestion(List<Map<String, Object>> filas,
                                              Map<String, Object> resumen,
                                              String periodo) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 40, 40, 50, 40);

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            escribirCabecera(doc, "INFORME FINAL DE GESTIÓN");

            // Periodo y resumen
            Font fuenteInfo = FontFactory.getFont(FontFactory.HELVETICA, 10,
                    new java.awt.Color(80, 80, 80));
            Paragraph infoPeriodo = new Paragraph("Periodo: " + periodo, fuenteInfo);
            infoPeriodo.setSpacingAfter(4);
            doc.add(infoPeriodo);

            long trasladosComp = toLong(resumen.get("trasladosCompletados"));
            long recogidasComp = toLong(resumen.get("recogidasCompletadas"));
            long lersDistintos = toLong(resumen.get("codigosLerDistintos"));
            Paragraph infoResumen = new Paragraph(
                "Traslados completados: " + trasladosComp
                + "  |  Recogidas completadas: " + recogidasComp
                + "  |  Códigos LER distintos: " + lersDistintos,
                fuenteInfo);
            infoResumen.setSpacingAfter(14);
            doc.add(infoResumen);

            // Tabla de datos por LER
            String[] cabeceras = {"Código LER", "Descripción", "Cantidad total", "Unidad",
                                   "Traslados", "Gestores únicos"};
            float[] anchos = {12f, 30f, 14f, 10f, 14f, 14f};
            PdfPTable tabla = new PdfPTable(cabeceras.length);
            tabla.setWidthPercentage(100);
            tabla.setWidths(anchos);

            Font fuenteCab = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, java.awt.Color.WHITE);
            for (String h : cabeceras) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fuenteCab));
                cell.setBackgroundColor(COLOR_CABECERA);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabla.addCell(cell);
            }

            Font fuenteDato = FontFactory.getFont(FontFactory.HELVETICA, 9);
            boolean par = true;
            for (Map<String, Object> fila : filas) {
                java.awt.Color bg = par ? COLOR_FILA_PAR : java.awt.Color.WHITE;
                agregarCeldaInforme(tabla, str(fila.get("codigoLER")), fuenteDato, bg, Element.ALIGN_LEFT);
                agregarCeldaInforme(tabla, str(fila.get("descripcion")), fuenteDato, bg, Element.ALIGN_LEFT);
                agregarCeldaInforme(tabla, str(fila.get("cantidadTotal")), fuenteDato, bg, Element.ALIGN_RIGHT);
                agregarCeldaInforme(tabla, str(fila.get("unidad")), fuenteDato, bg, Element.ALIGN_CENTER);
                agregarCeldaInforme(tabla, str(fila.get("trasladosCompletados")), fuenteDato, bg, Element.ALIGN_CENTER);
                agregarCeldaInforme(tabla, str(fila.get("gestoresUnicos")), fuenteDato, bg, Element.ALIGN_CENTER);
                par = !par;
            }

            doc.add(tabla);

            // Pie
            Font fuentePie = FontFactory.getFont(FontFactory.HELVETICA, 8,
                    new java.awt.Color(150, 150, 150));
            Paragraph pie = new Paragraph(
                "Generado por " + nombreEmpresa() + " · " + LocalDate.now().format(FORMATO_FECHA_CORTA),
                fuentePie);
            pie.setAlignment(Element.ALIGN_CENTER);
            pie.setSpacingBefore(20);
            doc.add(pie);

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de Informe Final de Gestión", e);
        } finally {
            doc.close();
        }

        return out.toByteArray();
    }

    private void agregarCeldaInforme(PdfPTable tabla, String valor, Font fuente,
                                      java.awt.Color fondo, int alineacion) {
        PdfPCell cell = new PdfPCell(new Phrase(valor, fuente));
        cell.setBackgroundColor(fondo);
        cell.setPadding(5);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setHorizontalAlignment(alineacion);
        tabla.addCell(cell);
    }

    private static final DateTimeFormatter FORMATO_FECHA_CORTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static String str(Object o) { return o == null ? "" : String.valueOf(o); }

    private static long toLong(Object o) {
        if (o == null) return 0;
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(o.toString()); } catch (NumberFormatException e) { return 0; }
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
