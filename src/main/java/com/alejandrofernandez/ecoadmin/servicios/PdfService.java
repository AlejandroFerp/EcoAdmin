package com.alejandrofernandez.ecoadmin.servicios;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.Direccion;
import com.alejandrofernandez.ecoadmin.modelo.Empresa;
import com.alejandrofernandez.ecoadmin.modelo.PerfilTransportista;
import com.alejandrofernandez.ecoadmin.modelo.Residuo;
import com.alejandrofernandez.ecoadmin.modelo.Ruta;
import com.alejandrofernandez.ecoadmin.modelo.Traslado;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.repository.EmpresaRepository;
import com.alejandrofernandez.ecoadmin.repository.PerfilTransportistaRepository;
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

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final java.awt.Color COLOR_PRIMARIO = new java.awt.Color(26, 107, 60);
    private static final java.awt.Color COLOR_SECCION = new java.awt.Color(240, 248, 243);
    private static final java.awt.Color COLOR_BORDE = new java.awt.Color(180, 180, 180);
    private static final java.awt.Color COLOR_LABEL = new java.awt.Color(80, 80, 80);

    private static final Font F_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, java.awt.Color.WHITE);
    private static final Font F_TITULO_EMPRESA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new java.awt.Color(26, 107, 60));
    private static final Font F_DATOS_EMPRESA = FontFactory.getFont(FontFactory.HELVETICA, 8, new java.awt.Color(80, 80, 80));
    private static final Font F_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new java.awt.Color(26, 107, 60));
    private static final Font F_SECCION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, java.awt.Color.WHITE);
    private static final Font F_LABEL = FontFactory.getFont(FontFactory.HELVETICA, 7, new java.awt.Color(80, 80, 80));
    private static final Font F_VALOR = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font F_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font F_PIE = FontFactory.getFont(FontFactory.HELVETICA, 7, new java.awt.Color(120, 120, 120));

    private final EmpresaRepository empresaRepo;
    private final PerfilTransportistaRepository perfilTransportistaRepo;

    public PdfService(EmpresaRepository empresaRepo, PerfilTransportistaRepository perfilTransportistaRepo) {
        this.empresaRepo = empresaRepo;
        this.perfilTransportistaRepo = perfilTransportistaRepo;
    }

    private Empresa obtenerEmpresa() {
        return empresaRepo.findAll().stream().findFirst().orElse(null);
    }

    private String nombreEmpresa() {
        Empresa e = obtenerEmpresa();
        return (e != null && e.getNombre() != null && !e.getNombre().isBlank()) ? e.getNombre() : "EcoAdmin";
    }

    // ─── Métodos públicos por tipo de documento ─────────────────────────────

    public byte[] generarCartaDePorte(Traslado traslado) {
        return generarDocumentoIdentificacion(traslado);
    }

    public byte[] generarNotificacionTraslado(Traslado traslado) {
        return generarNotificacionPrevia(traslado);
    }

    public byte[] generarCertificadoRecepcion(Traslado traslado) {
        return generarFichaAceptacionPdf(traslado);
    }

    public byte[] generarFichaAceptacion(Traslado traslado) {
        return generarFichaAceptacionPdf(traslado);
    }

    public byte[] generarHojaSeguimiento(Traslado traslado) {
        return generarHojaSeguimientoPdf(traslado);
    }

    public byte[] generarInformeDocumento(Traslado traslado) {
        return generarInformeTrasladoPdf(traslado);
    }

    public byte[] generarDocumentoContrato(Traslado traslado) {
        return generarContratoPdf(traslado);
    }

    // =========================================================================
    // NOTIFICACIÓN PREVIA DE TRASLADO
    // Inspirado en el modelo oficial E-3/L-35 del MITECO
    // =========================================================================

    private byte[] generarNotificacionPrevia(Traslado traslado) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 30, 30, 30, 30);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            // Cabecera del documento
            escribirCabeceraDocumento(doc, "NOTIFICACIÓN PREVIA DE TRASLADO",
                    "Modelo conforme al R.D. 553/2020 — Artículo 10");

            // 1. Datos del productor/poseedor
            doc.add(seccionTitulo("1. PRODUCTOR / POSEEDOR DE RESIDUOS"));
            doc.add(tablaCentro(traslado.getCentroProductor(), "Productor"));

            // 2. Datos del gestor destinatario
            doc.add(seccionTitulo("2. GESTOR AUTORIZADO DE DESTINO"));
            doc.add(tablaCentro(traslado.getCentroGestor(), "Gestor"));

            // 3. Transportista
            doc.add(seccionTitulo("3. TRANSPORTISTA AUTORIZADO"));
            doc.add(tablaTransportista(traslado));

            // 4. Residuo
            doc.add(seccionTitulo("4. IDENTIFICACIÓN DEL RESIDUO"));
            doc.add(tablaResiduo(traslado.getResiduo()));

            // 5. Datos del traslado
            doc.add(seccionTitulo("5. DATOS DEL TRASLADO"));
            doc.add(tablaDatosTraslado(traslado));

            // 6. Ruta prevista
            if (traslado.getRuta() != null) {
                doc.add(seccionTitulo("6. ITINERARIO PREVISTO"));
                doc.add(tablaRuta(traslado.getRuta()));
            }

            // Zona de firmas
            doc.add(zonaFirmas("Productor/Poseedor", "Transportista", "Gestor de destino"));

            // Pie de documento
            escribirPieDocumento(doc, traslado);

        } catch (Exception e) {
            throw new RuntimeException("Error generando Notificación Previa para traslado " + traslado.getId(), e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    // =========================================================================
    // DOCUMENTO DE IDENTIFICACIÓN (DI) / CARTA DE PORTE
    // Inspirado en el modelo oficial del MITECO para transporte de RP
    // =========================================================================

    private byte[] generarDocumentoIdentificacion(Traslado traslado) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 30, 30, 30, 30);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            escribirCabeceraDocumento(doc, "DOCUMENTO DE IDENTIFICACIÓN",
                    "Documento que acompaña al residuo durante su transporte — R.D. 553/2020");

            // 1. Productor
            doc.add(seccionTitulo("1. PRODUCTOR / POSEEDOR"));
            doc.add(tablaCentro(traslado.getCentroProductor(), "Productor"));

            // 2. Gestor
            doc.add(seccionTitulo("2. DESTINATARIO (GESTOR AUTORIZADO)"));
            doc.add(tablaCentro(traslado.getCentroGestor(), "Gestor"));

            // 3. Transportista
            doc.add(seccionTitulo("3. TRANSPORTISTA"));
            doc.add(tablaTransportista(traslado));

            // 4. Residuo
            doc.add(seccionTitulo("4. RESIDUO TRANSPORTADO"));
            doc.add(tablaResiduo(traslado.getResiduo()));

            // 5. Fechas y estado
            doc.add(seccionTitulo("5. DATOS DE TRANSPORTE"));
            doc.add(tablaDatosTransporte(traslado));

            // 6. Ruta
            if (traslado.getRuta() != null) {
                doc.add(seccionTitulo("6. RECORRIDO"));
                doc.add(tablaRuta(traslado.getRuta()));
            }

            // 7. Observaciones
            if (traslado.getObservaciones() != null && !traslado.getObservaciones().isBlank()) {
                doc.add(seccionTitulo("7. OBSERVACIONES"));
                Paragraph obs = new Paragraph(traslado.getObservaciones(), F_NORMAL);
                obs.setSpacingBefore(4);
                obs.setSpacingAfter(10);
                doc.add(obs);
            }

            // Firmas
            doc.add(zonaFirmas("Remitente", "Transportista", "Destinatario"));

            escribirPieDocumento(doc, traslado);

        } catch (Exception e) {
            throw new RuntimeException("Error generando DI para traslado " + traslado.getId(), e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    // =========================================================================
    // FICHA DE ACEPTACIÓN / CERTIFICADO DE RECEPCIÓN
    // =========================================================================

    private byte[] generarFichaAceptacionPdf(Traslado traslado) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 30, 30, 30, 30);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            escribirCabeceraDocumento(doc, "FICHA DE ACEPTACIÓN DE RESIDUOS",
                    "Documento acreditativo de recepción conforme del residuo");

            // Gestor que acepta
            doc.add(seccionTitulo("1. INSTALACIÓN RECEPTORA (GESTOR)"));
            doc.add(tablaCentro(traslado.getCentroGestor(), "Gestor"));

            // Productor de origen
            doc.add(seccionTitulo("2. ORIGEN DEL RESIDUO (PRODUCTOR)"));
            doc.add(tablaCentro(traslado.getCentroProductor(), "Productor"));

            // Residuo aceptado
            doc.add(seccionTitulo("3. RESIDUO ACEPTADO"));
            doc.add(tablaResiduo(traslado.getResiduo()));

            // Datos de recepción
            doc.add(seccionTitulo("4. DATOS DE RECEPCIÓN"));
            PdfPTable t = new PdfPTable(4);
            t.setWidthPercentage(100);
            t.setWidths(new float[]{25, 25, 25, 25});
            t.setSpacingAfter(12);
            addCampo(t, "Fecha de entrega", formatFechaHora(traslado.getFechaEntrega()));
            addCampo(t, "Estado traslado", traslado.getEstado() != null ? traslado.getEstado().name() : "—");
            addCampo(t, "Código traslado", traslado.getCodigo() != null ? traslado.getCodigo() : "—");
            addCampo(t, "Transportista", traslado.getTransportista() != null ? traslado.getTransportista().getNombre() : "—");
            doc.add(t);

            // Firmas
            doc.add(zonaFirmas("Gestor receptor", "Transportista", null));

            escribirPieDocumento(doc, traslado);

        } catch (Exception e) {
            throw new RuntimeException("Error generando Ficha Aceptación para traslado " + traslado.getId(), e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    // =========================================================================
    // HOJA DE SEGUIMIENTO
    // =========================================================================

    private byte[] generarHojaSeguimientoPdf(Traslado traslado) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 30, 30, 30, 30);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            escribirCabeceraDocumento(doc, "HOJA DE SEGUIMIENTO DE RESIDUOS PELIGROSOS",
                    "Seguimiento del transporte desde origen hasta destino final");

            // Partes implicadas
            doc.add(seccionTitulo("1. PRODUCTOR"));
            doc.add(tablaCentro(traslado.getCentroProductor(), "Productor"));

            doc.add(seccionTitulo("2. TRANSPORTISTA"));
            doc.add(tablaTransportista(traslado));

            doc.add(seccionTitulo("3. GESTOR DE DESTINO"));
            doc.add(tablaCentro(traslado.getCentroGestor(), "Gestor"));

            // Residuo
            doc.add(seccionTitulo("4. RESIDUO"));
            doc.add(tablaResiduo(traslado.getResiduo()));

            // Cronología
            doc.add(seccionTitulo("5. CRONOLOGÍA DEL TRASLADO"));
            PdfPTable crono = new PdfPTable(4);
            crono.setWidthPercentage(100);
            crono.setWidths(new float[]{25, 25, 25, 25});
            crono.setSpacingAfter(12);
            addCampo(crono, "Creación", formatFechaHora(traslado.getFechaCreacion()));
            addCampo(crono, "Inicio transporte", formatFechaHora(traslado.getFechaInicioTransporte()));
            addCampo(crono, "Entrega", formatFechaHora(traslado.getFechaEntrega()));
            addCampo(crono, "Estado actual", traslado.getEstado() != null ? traslado.getEstado().name() : "—");
            doc.add(crono);

            // Ruta
            if (traslado.getRuta() != null) {
                doc.add(seccionTitulo("6. ITINERARIO"));
                doc.add(tablaRuta(traslado.getRuta()));
            }

            // Firmas
            doc.add(zonaFirmas("Productor", "Transportista", "Gestor"));

            escribirPieDocumento(doc, traslado);

        } catch (Exception e) {
            throw new RuntimeException("Error generando Hoja Seguimiento para traslado " + traslado.getId(), e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    // =========================================================================
    // INFORME DEL TRASLADO
    // =========================================================================

    private byte[] generarInformeTrasladoPdf(Traslado traslado) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 30, 30, 30, 30);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            escribirCabeceraDocumento(doc, "INFORME DE GESTIÓN DEL TRASLADO",
                    "Resumen completo del traslado de residuos");

            // Resumen
            doc.add(seccionTitulo("1. DATOS GENERALES"));
            PdfPTable gen = new PdfPTable(4);
            gen.setWidthPercentage(100);
            gen.setWidths(new float[]{25, 25, 25, 25});
            gen.setSpacingAfter(12);
            addCampo(gen, "Código", traslado.getCodigo() != null ? traslado.getCodigo() : "—");
            addCampo(gen, "Estado", traslado.getEstado() != null ? traslado.getEstado().name() : "—");
            addCampo(gen, "Fecha creación", formatFechaHora(traslado.getFechaCreacion()));
            addCampo(gen, "Fecha entrega", formatFechaHora(traslado.getFechaEntrega()));
            doc.add(gen);

            // Partes
            doc.add(seccionTitulo("2. PRODUCTOR"));
            doc.add(tablaCentro(traslado.getCentroProductor(), "Productor"));

            doc.add(seccionTitulo("3. GESTOR"));
            doc.add(tablaCentro(traslado.getCentroGestor(), "Gestor"));

            doc.add(seccionTitulo("4. TRANSPORTISTA"));
            doc.add(tablaTransportista(traslado));

            // Residuo
            doc.add(seccionTitulo("5. RESIDUO"));
            doc.add(tablaResiduo(traslado.getResiduo()));

            // Observaciones
            if (traslado.getObservaciones() != null && !traslado.getObservaciones().isBlank()) {
                doc.add(seccionTitulo("6. OBSERVACIONES"));
                Paragraph obs = new Paragraph(traslado.getObservaciones(), F_NORMAL);
                obs.setSpacingBefore(4);
                obs.setSpacingAfter(10);
                doc.add(obs);
            }

            escribirPieDocumento(doc, traslado);

        } catch (Exception e) {
            throw new RuntimeException("Error generando Informe para traslado " + traslado.getId(), e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    // =========================================================================
    // CONTRATO / ACUERDO
    // =========================================================================

    private byte[] generarContratoPdf(Traslado traslado) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 30, 30, 30, 30);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            escribirCabeceraDocumento(doc, "CONTRATO DE TRATAMIENTO DE RESIDUOS",
                    "Acuerdo entre productor y gestor para el tratamiento de residuos peligrosos");

            Empresa empresa = obtenerEmpresa();

            // Partes
            doc.add(seccionTitulo("1. PRIMERA PARTE — PRODUCTOR / POSEEDOR"));
            doc.add(tablaCentro(traslado.getCentroProductor(), "Productor"));

            doc.add(seccionTitulo("2. SEGUNDA PARTE — GESTOR AUTORIZADO"));
            doc.add(tablaCentro(traslado.getCentroGestor(), "Gestor"));

            // Objeto del contrato
            doc.add(seccionTitulo("3. OBJETO DEL CONTRATO"));
            Residuo r = traslado.getResiduo();
            String desc = "Gestión y tratamiento del residuo";
            if (r != null) {
                desc += " con código LER " + nvl(r.getCodigoLER(), "—");
                if (r.getDescripcion() != null) desc += " (" + r.getDescripcion() + ")";
                desc += ", cantidad estimada: " + r.getCantidad() + " " + nvl(r.getUnidad(), "kg");
            }
            desc += ".";
            Paragraph objPar = new Paragraph(desc, F_NORMAL);
            objPar.setSpacingBefore(4);
            objPar.setSpacingAfter(10);
            doc.add(objPar);

            // Autorizaciones
            doc.add(seccionTitulo("4. AUTORIZACIONES"));
            PdfPTable aut = new PdfPTable(2);
            aut.setWidthPercentage(100);
            aut.setWidths(new float[]{50, 50});
            aut.setSpacingAfter(12);
            addCampo(aut, "Aut. Gestor", empresa != null ? nvl(empresa.getAutorizacionGestor(), "—") : "—");
            addCampo(aut, "Aut. Transportista", empresa != null ? nvl(empresa.getAutorizacionTransportista(), "—") : "—");
            doc.add(aut);

            // Firmas
            doc.add(zonaFirmas("El Productor", "El Gestor", null));

            escribirPieDocumento(doc, traslado);

        } catch (Exception e) {
            throw new RuntimeException("Error generando Contrato para traslado " + traslado.getId(), e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    // ─── Informe Final de Gestion (tabla LER) ───────────────────────────────

    public byte[] generarInformeFinalGestion(List<Map<String, Object>> filas,
                                              Map<String, Object> resumen,
                                              String periodo) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            escribirCabeceraDocumento(doc, "INFORME FINAL DE GESTIÓN",
                    "Resumen de actividad por código LER — Periodo: " + periodo);

            // Resumen general
            long trasladosComp = toLong(resumen.get("trasladosCompletados"));
            long recogidasComp = toLong(resumen.get("recogidasCompletadas"));
            long lersDistintos = toLong(resumen.get("codigosLerDistintos"));
            PdfPTable resumenTab = new PdfPTable(3);
            resumenTab.setWidthPercentage(60);
            resumenTab.setHorizontalAlignment(Element.ALIGN_CENTER);
            resumenTab.setSpacingAfter(14);
            addCampo(resumenTab, "Traslados completados", String.valueOf(trasladosComp));
            addCampo(resumenTab, "Recogidas completadas", String.valueOf(recogidasComp));
            addCampo(resumenTab, "Códigos LER distintos", String.valueOf(lersDistintos));
            doc.add(resumenTab);

            // Tabla de datos por LER
            String[] cabeceras = {"Código LER", "Descripción", "Cantidad total", "Unidad", "Traslados", "Gestores únicos"};
            float[] anchos = {12f, 30f, 14f, 10f, 14f, 14f};
            PdfPTable tabla = new PdfPTable(cabeceras.length);
            tabla.setWidthPercentage(100);
            tabla.setWidths(anchos);

            for (String h : cabeceras) {
                PdfPCell cell = new PdfPCell(new Phrase(h, F_SECCION));
                cell.setBackgroundColor(COLOR_PRIMARIO);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabla.addCell(cell);
            }

            boolean par = true;
            for (Map<String, Object> fila : filas) {
                java.awt.Color bg = par ? COLOR_SECCION : java.awt.Color.WHITE;
                agregarCeldaInforme(tabla, str(fila.get("codigoLER")), F_NORMAL, bg, Element.ALIGN_LEFT);
                agregarCeldaInforme(tabla, str(fila.get("descripcion")), F_NORMAL, bg, Element.ALIGN_LEFT);
                agregarCeldaInforme(tabla, str(fila.get("cantidadTotal")), F_NORMAL, bg, Element.ALIGN_RIGHT);
                agregarCeldaInforme(tabla, str(fila.get("unidad")), F_NORMAL, bg, Element.ALIGN_CENTER);
                agregarCeldaInforme(tabla, str(fila.get("trasladosCompletados")), F_NORMAL, bg, Element.ALIGN_CENTER);
                agregarCeldaInforme(tabla, str(fila.get("gestoresUnicos")), F_NORMAL, bg, Element.ALIGN_CENTER);
                par = !par;
            }
            doc.add(tabla);

            // Pie
            Paragraph pie = new Paragraph(
                    "Generado por " + nombreEmpresa() + " · " + LocalDate.now().format(FMT_FECHA), F_PIE);
            pie.setAlignment(Element.ALIGN_CENTER);
            pie.setSpacingBefore(16);
            doc.add(pie);

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de Informe Final de Gestión", e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    // =========================================================================
    // COMPONENTES REUTILIZABLES — Estilo formulario oficial con recuadros
    // =========================================================================

    private void escribirCabeceraDocumento(Document doc, String titulo, String subtitulo) {
        Empresa empresa = obtenerEmpresa();

        // Recuadro exterior del encabezado
        PdfPTable header = new PdfPTable(new float[]{65, 35});
        header.setWidthPercentage(100);
        header.setSpacingAfter(4);

        // Celda izquierda: empresa + datos fiscales
        PdfPCell celdaEmpresa = new PdfPCell();
        celdaEmpresa.setBorder(Rectangle.BOX);
        celdaEmpresa.setBorderWidth(1.5f);
        celdaEmpresa.setBorderColor(COLOR_PRIMARIO);
        celdaEmpresa.setPadding(8);
        celdaEmpresa.setBackgroundColor(new java.awt.Color(245, 250, 245));

        String nombreStr = (empresa != null && empresa.getNombre() != null) ? empresa.getNombre() : "EcoAdmin";
        celdaEmpresa.addElement(new Paragraph(nombreStr, F_TITULO_EMPRESA));

        if (empresa != null) {
            StringBuilder datos = new StringBuilder();
            if (empresa.getCif() != null && !empresa.getCif().isBlank())
                datos.append("CIF: ").append(empresa.getCif()).append("  ");
            if (empresa.getNima() != null && !empresa.getNima().isBlank())
                datos.append("NIMA: ").append(empresa.getNima()).append("  ");
            if (empresa.getTelefono() != null && !empresa.getTelefono().isBlank())
                datos.append("Tel: ").append(empresa.getTelefono());
            if (!datos.isEmpty()) {
                celdaEmpresa.addElement(new Paragraph(datos.toString(), F_DATOS_EMPRESA));
            }
            Direccion dirFiscal = empresa.getDireccionFiscal();
            if (dirFiscal != null) {
                celdaEmpresa.addElement(new Paragraph(formatDireccion(dirFiscal), F_DATOS_EMPRESA));
            }
        }
        header.addCell(celdaEmpresa);

        // Celda derecha: tipo de documento
        PdfPCell celdaTipo = new PdfPCell();
        celdaTipo.setBorder(Rectangle.BOX);
        celdaTipo.setBorderWidth(2f);
        celdaTipo.setBorderColor(COLOR_PRIMARIO);
        celdaTipo.setBackgroundColor(COLOR_PRIMARIO);
        celdaTipo.setPadding(10);
        celdaTipo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celdaTipo.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph pTitulo = new Paragraph(titulo, F_TITULO);
        pTitulo.setAlignment(Element.ALIGN_CENTER);
        celdaTipo.addElement(pTitulo);

        if (subtitulo != null) {
            Font fRef = FontFactory.getFont(FontFactory.HELVETICA, 7, new java.awt.Color(200, 230, 200));
            Paragraph pRef = new Paragraph(subtitulo, fRef);
            pRef.setAlignment(Element.ALIGN_CENTER);
            celdaTipo.addElement(pRef);
        }
        header.addCell(celdaTipo);
        doc.add(header);

        // Línea separadora
        PdfPTable sep = new PdfPTable(1);
        sep.setWidthPercentage(100);
        sep.setSpacingAfter(8);
        PdfPCell linea = new PdfPCell();
        linea.setBorder(Rectangle.BOTTOM);
        linea.setBorderWidth(0.5f);
        linea.setBorderColor(COLOR_PRIMARIO);
        linea.setFixedHeight(4);
        sep.addCell(linea);
        doc.add(sep);
    }

    /**
     * Título de sección con número, fondo oscuro y borde grueso que envuelve visualmente el bloque.
     */
    private PdfPTable seccionTitulo(String texto) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(6);
        t.setSpacingAfter(2);
        PdfPCell cell = new PdfPCell(new Phrase(texto, F_SECCION));
        cell.setBackgroundColor(COLOR_PRIMARIO);
        cell.setPadding(5);
        cell.setPaddingLeft(8);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(1.2f);
        cell.setBorderColor(COLOR_PRIMARIO);
        t.addCell(cell);
        return t;
    }

    private PdfPTable tablaCentro(Centro centro, String rol) {
        PdfPTable wrapper = new PdfPTable(1);
        wrapper.setWidthPercentage(100);
        wrapper.setSpacingAfter(4);

        // Tabla interna con campos
        PdfPTable t = new PdfPTable(4);
        t.setWidthPercentage(100);
        try { t.setWidths(new float[]{30, 20, 25, 25}); } catch (Exception ignored) {}

        String nombre = "—";
        String nima = "—";
        String telefono = "—";
        String email = "—";
        String direccion = "—";
        String contacto = "—";

        if (centro != null) {
            nombre = nvl(centro.getNombre(), "—");
            nima = nvl(centro.getNima(), "—");
            telefono = nvl(centro.getTelefono(), "—");
            email = nvl(centro.getEmail(), "—");
            contacto = nvl(centro.getNombreContacto(), "—");
            Direccion dir = centro.getDireccion();
            if (dir != null) {
                direccion = formatDireccion(dir);
            }
        }

        addCampo(t, "Razón social / Nombre", nombre);
        addCampo(t, "NIMA", nima);
        addCampo(t, "Teléfono", telefono);
        addCampo(t, "Email", email);

        // Segunda fila
        PdfPCell cDireccion = campoCelda("Dirección", direccion);
        cDireccion.setColspan(3);
        t.addCell(cDireccion);
        addCampo(t, "Contacto", contacto);

        // Envolver en celda con borde grueso
        PdfPCell wrapperCell = new PdfPCell(t);
        wrapperCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
        wrapperCell.setBorderWidth(1.2f);
        wrapperCell.setBorderColor(COLOR_PRIMARIO);
        wrapperCell.setPadding(0);
        wrapper.addCell(wrapperCell);

        return wrapper;
    }

    private PdfPTable tablaTransportista(Traslado traslado) {
        PdfPTable wrapper = new PdfPTable(1);
        wrapper.setWidthPercentage(100);
        wrapper.setSpacingAfter(4);

        PdfPTable t = new PdfPTable(4);
        t.setWidthPercentage(100);
        try { t.setWidths(new float[]{30, 20, 25, 25}); } catch (Exception ignored) {}

        Usuario transportista = traslado.getTransportista();
        String nombre = "—";
        String dni = "—";
        String telefono = "—";
        String matricula = "—";

        if (transportista != null) {
            nombre = nvl(transportista.getNombre(), "—");
            dni = nvl(transportista.getDni(), "—");
            telefono = nvl(transportista.getTelefono(), "—");

            Optional<PerfilTransportista> perfil = perfilTransportistaRepo.findByUsuario(transportista);
            if (perfil.isPresent()) {
                matricula = nvl(perfil.get().getMatricula(), "—");
            }
        }

        addCampo(t, "Nombre / Razón social", nombre);
        addCampo(t, "DNI/CIF", dni);
        addCampo(t, "Teléfono", telefono);
        addCampo(t, "Matrícula vehículo", matricula);

        PdfPCell wrapperCell = new PdfPCell(t);
        wrapperCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
        wrapperCell.setBorderWidth(1.2f);
        wrapperCell.setBorderColor(COLOR_PRIMARIO);
        wrapperCell.setPadding(0);
        wrapper.addCell(wrapperCell);

        return wrapper;
    }

    private PdfPTable tablaResiduo(Residuo residuo) {
        PdfPTable wrapper = new PdfPTable(1);
        wrapper.setWidthPercentage(100);
        wrapper.setSpacingAfter(4);

        PdfPTable t = new PdfPTable(4);
        t.setWidthPercentage(100);
        try { t.setWidths(new float[]{20, 35, 20, 25}); } catch (Exception ignored) {}

        if (residuo == null) {
            PdfPCell empty = new PdfPCell(new Phrase("Sin residuo asociado", F_NORMAL));
            empty.setColspan(4);
            empty.setPadding(8);
            empty.setBorder(Rectangle.BOX);
            empty.setBorderColor(COLOR_BORDE);
            t.addCell(empty);
        } else {
            addCampo(t, "Código LER", nvl(residuo.getCodigoLER(), "—"));
            addCampo(t, "Descripción", nvl(residuo.getDescripcion(), "—"));
            addCampo(t, "Cantidad", residuo.getCantidad() + " " + nvl(residuo.getUnidad(), "kg"));
            addCampo(t, "Estado físico", nvl(residuo.getEstado(), "—"));
        }

        PdfPCell wrapperCell = new PdfPCell(t);
        wrapperCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
        wrapperCell.setBorderWidth(1.2f);
        wrapperCell.setBorderColor(COLOR_PRIMARIO);
        wrapperCell.setPadding(0);
        wrapper.addCell(wrapperCell);

        return wrapper;
    }

    private PdfPTable tablaDatosTraslado(Traslado traslado) {
        PdfPTable wrapper = new PdfPTable(1);
        wrapper.setWidthPercentage(100);
        wrapper.setSpacingAfter(4);

        PdfPTable t = new PdfPTable(4);
        t.setWidthPercentage(100);
        try { t.setWidths(new float[]{25, 25, 25, 25}); } catch (Exception ignored) {}

        addCampo(t, "Código traslado", nvl(traslado.getCodigo(), "—"));
        addCampo(t, "Estado", traslado.getEstado() != null ? traslado.getEstado().name() : "—");
        addCampo(t, "Fecha prevista inicio", formatFechaHora(traslado.getFechaProgramadaInicio()));
        addCampo(t, "Fecha prevista fin", formatFechaHora(traslado.getFechaProgramadaFin()));

        PdfPCell wrapperCell = new PdfPCell(t);
        wrapperCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
        wrapperCell.setBorderWidth(1.2f);
        wrapperCell.setBorderColor(COLOR_PRIMARIO);
        wrapperCell.setPadding(0);
        wrapper.addCell(wrapperCell);

        return wrapper;
    }

    private PdfPTable tablaDatosTransporte(Traslado traslado) {
        PdfPTable wrapper = new PdfPTable(1);
        wrapper.setWidthPercentage(100);
        wrapper.setSpacingAfter(4);

        PdfPTable t = new PdfPTable(4);
        t.setWidthPercentage(100);
        try { t.setWidths(new float[]{25, 25, 25, 25}); } catch (Exception ignored) {}

        addCampo(t, "Código traslado", nvl(traslado.getCodigo(), "—"));
        addCampo(t, "Estado", traslado.getEstado() != null ? traslado.getEstado().name() : "—");
        addCampo(t, "Inicio transporte", formatFechaHora(traslado.getFechaInicioTransporte()));
        addCampo(t, "Fecha entrega", formatFechaHora(traslado.getFechaEntrega()));

        PdfPCell wrapperCell = new PdfPCell(t);
        wrapperCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
        wrapperCell.setBorderWidth(1.2f);
        wrapperCell.setBorderColor(COLOR_PRIMARIO);
        wrapperCell.setPadding(0);
        wrapper.addCell(wrapperCell);

        return wrapper;
    }

    private PdfPTable tablaRuta(Ruta ruta) {
        PdfPTable wrapper = new PdfPTable(1);
        wrapper.setWidthPercentage(100);
        wrapper.setSpacingAfter(4);

        PdfPTable t = new PdfPTable(4);
        t.setWidthPercentage(100);
        try { t.setWidths(new float[]{30, 30, 20, 20}); } catch (Exception ignored) {}

        String origen = "—";
        String destino = "—";
        if (ruta.getOrigen() != null) origen = formatDireccion(ruta.getOrigen());
        if (ruta.getDestino() != null) destino = formatDireccion(ruta.getDestino());

        addCampo(t, "Origen", origen);
        addCampo(t, "Destino", destino);
        addCampo(t, "Distancia", ruta.getDistanciaKm() != null ? ruta.getDistanciaKm() + " km" : "—");
        addCampo(t, "Nombre ruta", nvl(ruta.getNombre(), "—"));

        PdfPCell wrapperCell = new PdfPCell(t);
        wrapperCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
        wrapperCell.setBorderWidth(1.2f);
        wrapperCell.setBorderColor(COLOR_PRIMARIO);
        wrapperCell.setPadding(0);
        wrapper.addCell(wrapperCell);

        return wrapper;
    }

    private PdfPTable zonaFirmas(String parte1, String parte2, String parte3) {
        int cols = parte3 != null ? 3 : 2;
        PdfPTable t = new PdfPTable(cols);
        t.setWidthPercentage(100);
        t.setSpacingBefore(14);
        t.setSpacingAfter(6);

        addCeldaFirma(t, parte1);
        addCeldaFirma(t, parte2);
        if (parte3 != null) addCeldaFirma(t, parte3);

        return t;
    }

    private void addCeldaFirma(PdfPTable t, String titulo) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(1f);
        cell.setBorderColor(COLOR_PRIMARIO);
        cell.setPadding(6);
        cell.setFixedHeight(70);

        Font fTitFirma = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, COLOR_PRIMARIO);
        Font fDetFirma = FontFactory.getFont(FontFactory.HELVETICA, 7, COLOR_LABEL);

        Paragraph pTit = new Paragraph(titulo, fTitFirma);
        pTit.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pTit);

        Paragraph pFirma = new Paragraph("\n\n\nFirma y sello", fDetFirma);
        pFirma.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pFirma);

        Paragraph pFecha = new Paragraph("Fecha: ___/___/______", fDetFirma);
        pFecha.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pFecha);

        t.addCell(cell);
    }

    private void escribirPieDocumento(Document doc, Traslado traslado) {
        Paragraph pie = new Paragraph(
                "Documento generado por " + nombreEmpresa()
                        + " · Traslado: " + (traslado.getCodigo() != null ? traslado.getCodigo() : "#" + traslado.getId())
                        + " · Fecha emisión: " + LocalDate.now().format(FMT_FECHA),
                F_PIE);
        pie.setAlignment(Element.ALIGN_CENTER);
        pie.setSpacingBefore(12);
        doc.add(pie);
    }

    // ─── Helpers para celdas de formulario ───────────────────────────────────

    private void addCampo(PdfPTable tabla, String label, String valor) {
        tabla.addCell(campoCelda(label, valor));
    }

    /**
     * Celda tipo casilla de formulario: borde completo, etiqueta pequeña arriba, valor en negrita abajo.
     */
    private PdfPCell campoCelda(String label, String valor) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(0.7f);
        cell.setBorderColor(COLOR_BORDE);
        cell.setPadding(3);
        cell.setPaddingBottom(5);
        cell.setBackgroundColor(java.awt.Color.WHITE);

        Paragraph pLabel = new Paragraph(label, F_LABEL);
        cell.addElement(pLabel);

        Paragraph pValor = new Paragraph(nvl(valor, "—"), F_VALOR);
        cell.addElement(pValor);

        return cell;
    }

    private void agregarCeldaInforme(PdfPTable tabla, String valor, Font fuente,
                                      java.awt.Color fondo, int alineacion) {
        PdfPCell cell = new PdfPCell(new Phrase(valor, fuente));
        cell.setBackgroundColor(fondo);
        cell.setPadding(5);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(COLOR_BORDE);
        cell.setHorizontalAlignment(alineacion);
        tabla.addCell(cell);
    }

    // ─── Utilidades ──────────────────────────────────────────────────────────

    private String formatDireccion(Direccion dir) {
        if (dir == null) return "—";
        StringBuilder sb = new StringBuilder();
        if (dir.getCalle() != null && !dir.getCalle().isBlank()) sb.append(dir.getCalle());
        if (dir.getCiudad() != null && !dir.getCiudad().isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(dir.getCiudad());
        }
        if (dir.getCodigoPostal() != null && !dir.getCodigoPostal().isBlank()) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append("(").append(dir.getCodigoPostal()).append(")");
        }
        if (dir.getProvincia() != null && !dir.getProvincia().isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(dir.getProvincia());
        }
        return sb.isEmpty() ? "—" : sb.toString();
    }

    private String formatFechaHora(LocalDateTime dt) {
        if (dt == null) return "—";
        return dt.format(FMT_FECHA_HORA);
    }

    private static String nvl(String val, String fallback) {
        return (val == null || val.isBlank()) ? fallback : val;
    }

    private static String str(Object o) { return o == null ? "" : String.valueOf(o); }

    private static long toLong(Object o) {
        if (o == null) return 0;
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(o.toString()); } catch (NumberFormatException e) { return 0; }
    }
}
