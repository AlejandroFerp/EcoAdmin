package com.iesdoctorbalmis.spring.controladores;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.iesdoctorbalmis.spring.modelo.Centro;
import com.iesdoctorbalmis.spring.modelo.Documento;
import com.iesdoctorbalmis.spring.modelo.EventoTraslado;
import com.iesdoctorbalmis.spring.modelo.Recogida;
import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;
import com.iesdoctorbalmis.spring.modelo.enums.TipoDocumento;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.DocumentoRepository;
import com.iesdoctorbalmis.spring.repository.RecogidaRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.TrasladoRepository;
import com.iesdoctorbalmis.spring.servicios.PdfService;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoints para informes dinamicos (Fase 1: filtros + tabla + export CSV).
 * Tipos soportados: traslados, residuos, centros, documentos.
 */
@Tag(name = "Informes", description = "Informes dinamicos con filtros y exportacion CSV")
@RestController
@RequestMapping("/api/informes")
@PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
public class InformesController {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TrasladoRepository trasladoRepo;
    private final ResiduoRepository residuoRepo;
    private final CentroRepository centroRepo;
    private final DocumentoRepository documentoRepo;
    private final RecogidaRepository recogidaRepo;
    private final PdfService pdfService;

    public InformesController(TrasladoRepository trasladoRepo,
                              ResiduoRepository residuoRepo,
                              CentroRepository centroRepo,
                              DocumentoRepository documentoRepo,
                              RecogidaRepository recogidaRepo,
                              PdfService pdfService) {
        this.trasladoRepo = trasladoRepo;
        this.residuoRepo = residuoRepo;
        this.centroRepo = centroRepo;
        this.documentoRepo = documentoRepo;
        this.recogidaRepo = recogidaRepo;
        this.pdfService = pdfService;
    }

    /** Devuelve un JSON {columns: [...], rows: [[...]]} para pintar en el cliente. */
    @GetMapping("/{tipo}")
    public ResponseEntity<Map<String, Object>> ejecutar(@PathVariable String tipo,
                                                         @RequestParam(required = false) String desde,
                                                         @RequestParam(required = false) String hasta,
                                                         @RequestParam(required = false) String estado,
                                                         @RequestParam(required = false) Long centroId,
                                                         @RequestParam(required = false) String codigoLER,
                                                         @RequestParam(required = false) String tipoDocumento) {
        ReportSpec spec = buildSpec(tipo, desde, hasta, estado, centroId, codigoLER, tipoDocumento);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("columns", spec.headers);
        body.put("rows", spec.rows.stream().map(spec::toRow).toList());
        body.put("total", spec.rows.size());
        return ResponseEntity.ok(body);
    }

    /** Descarga CSV con las mismas columnas/filas que el endpoint JSON. */
    @GetMapping("/{tipo}/csv")
    public ResponseEntity<StreamingResponseBody> descargarCsv(@PathVariable String tipo,
                                                               @RequestParam(required = false) String desde,
                                                               @RequestParam(required = false) String hasta,
                                                               @RequestParam(required = false) String estado,
                                                               @RequestParam(required = false) Long centroId,
                                                               @RequestParam(required = false) String codigoLER,
                                                               @RequestParam(required = false) String tipoDocumento) {
        ReportSpec spec = buildSpec(tipo, desde, hasta, estado, centroId, codigoLER, tipoDocumento);
        String filename = "informe-" + tipo + "-" + LocalDate.now() + ".csv";

        StreamingResponseBody body = out -> {
            // BOM UTF-8 para que Excel detecte la codificacion correctamente
            out.write(0xEF); out.write(0xBB); out.write(0xBF);
            try (Writer w = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                writeCsvLine(w, spec.headers);
                for (Object item : spec.rows) {
                    List<String> row = spec.toRow(item).stream().map(InformesController::stringify).toList();
                    writeCsvLine(w, row);
                }
            }
        };

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(body);
    }

    // ─── Construccion de la spec por tipo ─────────────────────────────────────

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ReportSpec buildSpec(String tipo,
                                  String desde, String hasta,
                                  String estado, Long centroId,
                                  String codigoLER, String tipoDocumento) {
        LocalDateTime desdeDt = parseFechaInicio(desde);
        LocalDateTime hastaDt = parseFechaFin(hasta);

        return switch (tipo == null ? "" : tipo.toLowerCase()) {
            case "traslados" -> {
                List<Traslado> data = trasladoRepo.findAll().stream()
                    .filter(t -> desdeDt == null || (t.getFechaCreacion() != null && !t.getFechaCreacion().isBefore(desdeDt)))
                    .filter(t -> hastaDt == null || (t.getFechaCreacion() != null && !t.getFechaCreacion().isAfter(hastaDt)))
                    .filter(t -> estado == null || estado.isBlank() || estado.equalsIgnoreCase(String.valueOf(t.getEstado())))
                    .filter(t -> centroId == null
                                 || (t.getCentroProductor() != null && centroId.equals(t.getCentroProductor().getId()))
                                 || (t.getCentroGestor() != null && centroId.equals(t.getCentroGestor().getId())))
                    .filter(t -> codigoLER == null || codigoLER.isBlank()
                                 || (t.getResiduo() != null && codigoLER.equalsIgnoreCase(t.getResiduo().getCodigoLER())))
                    .sorted(Comparator.comparing(Traslado::getFechaCreacion, Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();
                List<String> headers = List.of("ID", "Fecha creacion", "Estado", "Productor", "Gestor", "Codigo LER", "Residuo", "Transportista", "Inicio transporte", "Entrega", "Observaciones");
                Function<Object, List<Object>> mapper = obj -> {
                    Traslado t = (Traslado) obj;
                    return List.of(
                        nullSafe(t.getId()),
                        formatDateTime(t.getFechaCreacion()),
                        nullSafe(t.getEstado()),
                        nombreCentro(t.getCentroProductor()),
                        nombreCentro(t.getCentroGestor()),
                        t.getResiduo() != null ? nullSafe(t.getResiduo().getCodigoLER()) : "",
                        t.getResiduo() != null ? nullSafe(t.getResiduo().getDescripcion()) : "",
                        t.getTransportista() != null ? nullSafe(t.getTransportista().getNombre()) : "",
                        formatDateTime(t.getFechaInicioTransporte()),
                        formatDateTime(t.getFechaEntrega()),
                        nullSafe(t.getObservaciones())
                    );
                };
                yield new ReportSpec(headers, (List) data, mapper);
            }

            case "residuos" -> {
                List<Residuo> data = residuoRepo.findAll().stream()
                    .filter(r -> centroId == null || (r.getCentro() != null && centroId.equals(r.getCentro().getId())))
                    .filter(r -> codigoLER == null || codigoLER.isBlank() || codigoLER.equalsIgnoreCase(r.getCodigoLER()))
                    .filter(r -> estado == null || estado.isBlank() || estado.equalsIgnoreCase(r.getEstado()))
                    .sorted(Comparator.comparing(Residuo::getId))
                    .toList();
                List<String> headers = List.of("ID", "Codigo LER", "Descripcion", "Cantidad", "Unidad", "Estado", "Centro", "Entrada almacen", "Salida almacen", "Dias maximo");
                Function<Object, List<Object>> mapper = obj -> {
                    Residuo r = (Residuo) obj;
                    return List.of(
                        nullSafe(r.getId()),
                        nullSafe(r.getCodigoLER()),
                        nullSafe(r.getDescripcion()),
                        r.getCantidad(),
                        nullSafe(r.getUnidad()),
                        nullSafe(r.getEstado()),
                        nombreCentro(r.getCentro()),
                        formatDateTime(r.getFechaEntradaAlmacen()),
                        formatDateTime(r.getFechaSalidaAlmacen()),
                        nullSafe(r.getDiasMaximoAlmacenamiento())
                    );
                };
                yield new ReportSpec(headers, (List) data, mapper);
            }

            case "centros" -> {
                List<Centro> data = centroRepo.findAll().stream()
                    .filter(c -> centroId == null || centroId.equals(c.getId()))
                    .filter(c -> estado == null || estado.isBlank() || estado.equalsIgnoreCase(c.getTipo()))
                    .sorted(Comparator.comparing(Centro::getNombre, Comparator.nullsLast(String::compareToIgnoreCase)))
                    .toList();
                List<String> headers = List.of("ID", "Nombre", "Tipo", "NIMA", "Telefono", "Email", "Contacto", "Direccion", "Ciudad", "Provincia");
                Function<Object, List<Object>> mapper = obj -> {
                    Centro c = (Centro) obj;
                    String calle = c.getDireccion() != null ? nullSafe(c.getDireccion().getCalle()).toString() : "";
                    String ciudad = c.getDireccion() != null ? nullSafe(c.getDireccion().getCiudad()).toString() : "";
                    String prov = c.getDireccion() != null ? nullSafe(c.getDireccion().getProvincia()).toString() : "";
                    return List.of(
                        nullSafe(c.getId()),
                        nullSafe(c.getNombre()),
                        nullSafe(c.getTipo()),
                        nullSafe(c.getNima()),
                        nullSafe(c.getTelefono()),
                        nullSafe(c.getEmail()),
                        nullSafe(c.getNombreContacto()),
                        calle,
                        ciudad,
                        prov
                    );
                };
                yield new ReportSpec(headers, (List) data, mapper);
            }

            case "documentos" -> {
                LocalDate desdeD = desdeDt != null ? desdeDt.toLocalDate() : null;
                LocalDate hastaD = hastaDt != null ? hastaDt.toLocalDate() : null;
                List<Documento> data = documentoRepo.findAll().stream()
                    .filter(d -> tipoDocumento == null || tipoDocumento.isBlank() || tipoDocumento.equalsIgnoreCase(String.valueOf(d.getTipo())))
                    .filter(d -> estado == null || estado.isBlank() || estado.equalsIgnoreCase(String.valueOf(d.getEstado())))
                    .filter(d -> centroId == null
                                 || (d.getCentro() != null && centroId.equals(d.getCentro().getId()))
                                 || (d.getTraslado() != null && d.getTraslado().getCentroProductor() != null && centroId.equals(d.getTraslado().getCentroProductor().getId()))
                                 || (d.getTraslado() != null && d.getTraslado().getCentroGestor() != null && centroId.equals(d.getTraslado().getCentroGestor().getId())))
                    .filter(d -> desdeD == null || (d.getFechaEmision() != null && !d.getFechaEmision().isBefore(desdeD)))
                    .filter(d -> hastaD == null || (d.getFechaEmision() != null && !d.getFechaEmision().isAfter(hastaD)))
                    .sorted(Comparator.comparing(Documento::getId, Comparator.reverseOrder()))
                    .toList();
                List<String> headers = List.of("ID", "Referencia", "Tipo", "Estado", "Traslado", "Centro", "Fecha emision", "Vencimiento", "Cierre", "Observaciones");
                Function<Object, List<Object>> mapper = obj -> {
                    Documento d = (Documento) obj;
                    return List.of(
                        nullSafe(d.getId()),
                        nullSafe(d.getNumeroReferencia()),
                        nullSafe(d.getTipo()),
                        nullSafe(d.getEstado()),
                        d.getTraslado() != null ? nullSafe(d.getTraslado().getId()) : "",
                        nombreCentro(d.getCentro()),
                        d.getFechaEmision() != null ? d.getFechaEmision().format(FECHA) : "",
                        d.getFechaVencimiento() != null ? d.getFechaVencimiento().format(FECHA) : "",
                        d.getFechaCierre() != null ? d.getFechaCierre().format(FECHA) : "",
                        nullSafe(d.getObservaciones())
                    );
                };
                yield new ReportSpec(headers, (List) data, mapper);
            }

            case "almacen" -> {
                LocalDateTime ahora = LocalDateTime.now();
                List<Residuo> data = residuoRepo.findAll().stream()
                    .filter(r -> r.getFechaEntradaAlmacen() != null && r.getFechaSalidaAlmacen() == null)
                    .filter(r -> centroId == null || (r.getCentro() != null && centroId.equals(r.getCentro().getId())))
                    .filter(r -> codigoLER == null || codigoLER.isBlank() || codigoLER.equalsIgnoreCase(r.getCodigoLER()))
                    .sorted(Comparator.comparing(Residuo::getFechaEntradaAlmacen))
                    .toList();
                List<String> headers = List.of("Codigo", "LER", "Descripcion", "Cantidad", "Unidad", "Centro", "Entrada almacen", "Dias en almacen", "Dias maximo", "Alerta");
                Function<Object, List<Object>> mapper = obj -> {
                    Residuo r = (Residuo) obj;
                    long dias = ChronoUnit.DAYS.between(r.getFechaEntradaAlmacen(), ahora);
                    int max = r.getDiasMaximoAlmacenamiento() == null ? 180 : r.getDiasMaximoAlmacenamiento();
                    String alerta = dias > max ? "CRITICO" : (dias > max * 0.8 ? "AVISO" : "OK");
                    return List.of(
                        nullSafe(r.getCodigo() != null ? r.getCodigo() : r.getId()),
                        nullSafe(r.getCodigoLER()),
                        nullSafe(r.getDescripcion()),
                        r.getCantidad(),
                        nullSafe(r.getUnidad()),
                        nombreCentro(r.getCentro()),
                        formatDateTime(r.getFechaEntradaAlmacen()),
                        dias,
                        max,
                        alerta
                    );
                };
                yield new ReportSpec(headers, (List) data, mapper);
            }

            default -> throw new IllegalArgumentException("Tipo de informe desconocido: " + tipo
                + ". Valores validos: traslados, residuos, centros, documentos, almacen.");
        };
    }

    // ─── Informe inventario almacen (20.4) ────────────────────────────────────

    /**
     * Inventario actual del almacen: residuos que han entrado y aun no han salido.
     * Ordenados FIFO (mas antiguo primero). Incluye alerta FIFO.
     */
    @GetMapping("/inventario-almacen")
    public ResponseEntity<Map<String, Object>> inventarioAlmacen(
            @RequestParam(required = false) Long centroId,
            @RequestParam(required = false) String codigoLER) {

        LocalDateTime ahora = LocalDateTime.now();
        List<Residuo> data = residuoRepo.findAll().stream()
            .filter(r -> r.getFechaEntradaAlmacen() != null && r.getFechaSalidaAlmacen() == null)
            .filter(r -> centroId == null || (r.getCentro() != null && centroId.equals(r.getCentro().getId())))
            .filter(r -> codigoLER == null || codigoLER.isBlank() || codigoLER.equalsIgnoreCase(r.getCodigoLER()))
            .sorted(Comparator.comparing(Residuo::getFechaEntradaAlmacen))
            .toList();

        List<String> cols = List.of("Codigo", "LER", "Descripcion", "Cantidad", "Unidad", "Centro", "Entrada almacen", "Dias en almacen", "Dias maximo", "Alerta");
        List<List<Object>> rows = data.stream().map(r -> {
            long dias = ChronoUnit.DAYS.between(r.getFechaEntradaAlmacen(), ahora);
            int max = r.getDiasMaximoAlmacenamiento() == null ? 180 : r.getDiasMaximoAlmacenamiento();
            String alerta = dias > max ? "CRITICO" : (dias > max * 0.8 ? "AVISO" : "OK");
            return (List<Object>) List.<Object>of(
                nullSafe(r.getCodigo() != null ? r.getCodigo() : r.getId()),
                nullSafe(r.getCodigoLER()),
                nullSafe(r.getDescripcion()),
                r.getCantidad(),
                nullSafe(r.getUnidad()),
                nombreCentro(r.getCentro()),
                formatDateTime(r.getFechaEntradaAlmacen()),
                dias,
                max,
                alerta
            );
        }).toList();

        long criticos = rows.stream().filter(row -> "CRITICO".equals(row.get(9))).count();
        long avisos   = rows.stream().filter(row -> "AVISO".equals(row.get(9))).count();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("columns", cols);
        body.put("rows", rows);
        body.put("total", data.size());
        body.put("resumen", Map.of("total", data.size(), "criticos", criticos, "avisos", avisos));
        return ResponseEntity.ok(body);
    }

    // ─── Trazabilidad por residuo (20.5) ─────────────────────────────────────

    /**
     * Historia completa de un residuo: entrada almacen → recogidas → traslados → destino.
     */
    @GetMapping("/trazabilidad/{residuoId}")
    public ResponseEntity<Map<String, Object>> trazabilidad(@PathVariable Long residuoId) {
        return residuoRepo.findById(residuoId).map(r -> {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("residuo", Map.of(
                "id", r.getId(),
                "codigo", nullSafe(r.getCodigo()),
                "codigoLER", nullSafe(r.getCodigoLER()),
                "descripcion", nullSafe(r.getDescripcion()),
                "cantidad", r.getCantidad(),
                "unidad", nullSafe(r.getUnidad()),
                "centro", nombreCentro(r.getCentro()),
                "fechaEntradaAlmacen", formatDateTime(r.getFechaEntradaAlmacen()),
                "fechaSalidaAlmacen", formatDateTime(r.getFechaSalidaAlmacen())
            ));

            // Traslados asociados
            List<Map<String, Object>> traslados = trasladoRepo.findAll().stream()
                .filter(t -> t.getResiduo() != null && residuoId.equals(t.getResiduo().getId()))
                .sorted(Comparator.comparing(Traslado::getFechaCreacion, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", t.getId());
                    m.put("codigo", nullSafe(t.getCodigo()));
                    m.put("estado", nullSafe(t.getEstado()));
                    m.put("productor", nombreCentro(t.getCentroProductor()));
                    m.put("gestor", nombreCentro(t.getCentroGestor()));
                    m.put("transportista", t.getTransportista() != null ? nullSafe(t.getTransportista().getNombre()) : "");
                    m.put("fechaCreacion", formatDateTime(t.getFechaCreacion()));
                    m.put("fechaEntrega", formatDateTime(t.getFechaEntrega()));
                    // Historial de estados
                    List<Map<String, Object>> historial = t.getHistorial() == null ? List.of() :
                        t.getHistorial().stream()
                            .sorted(Comparator.comparing(EventoTraslado::getFecha, Comparator.nullsLast(Comparator.naturalOrder())))
                            .map(e -> Map.<String, Object>of(
                                "estadoAnterior", nullSafe(e.getEstadoAnterior()),
                                "estadoNuevo", nullSafe(e.getEstadoNuevo()),
                                "fecha", formatDateTime(e.getFecha()),
                                "comentario", nullSafe(e.getComentario())
                            )).toList();
                    m.put("historial", historial);
                    return m;
                }).toList();
            body.put("traslados", traslados);

            // Recogidas asociadas
            List<Map<String, Object>> recogidas = recogidaRepo.findAll().stream()
                .filter(rc -> rc.getResiduo() != null && residuoId.equals(rc.getResiduo().getId()))
                .map(rc -> Map.<String, Object>of(
                    "id", rc.getId(),
                    "codigo", nullSafe(rc.getCodigo()),
                    "estado", nullSafe(rc.getEstado()),
                    "centroOrigen", nombreCentro(rc.getCentroOrigen()),
                    "centroDestino", nombreCentro(rc.getCentroDestino()),
                    "fechaProgramada", rc.getFechaProgramada() != null ? rc.getFechaProgramada().format(FECHA) : "",
                    "fechaRealizada", rc.getFechaRealizada() != null ? rc.getFechaRealizada().format(FECHA) : ""
                )).toList();
            body.put("recogidas", recogidas);

            // Documentos asociados
            List<Map<String, Object>> documentos = documentoRepo.findAll().stream()
                .filter(d -> d.getTraslado() != null && d.getTraslado().getResiduo() != null
                          && residuoId.equals(d.getTraslado().getResiduo().getId()))
                .map(d -> Map.<String, Object>of(
                    "tipo", nullSafe(d.getTipo()),
                    "referencia", nullSafe(d.getNumeroReferencia()),
                    "estado", nullSafe(d.getEstado()),
                    "fechaEmision", d.getFechaEmision() != null ? d.getFechaEmision().format(FECHA) : ""
                )).toList();
            body.put("documentos", documentos);

            return ResponseEntity.ok(body);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── Informe Final de Gestion (20.6) ─────────────────────────────────────

    /**
     * Informe Final de Gestion: resumen cuantitativo por LER para un periodo dado.
     * Devuelve JSON; el cliente puede exportarlo a CSV desde el mecanismo estandar.
     */
    @GetMapping("/final-gestion")
    public ResponseEntity<Map<String, Object>> finalGestion(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {

        LocalDateTime desdeDt = parseFechaInicio(desde);
        LocalDateTime hastaDt = parseFechaFin(hasta);

        List<Traslado> traslados = trasladoRepo.findAll().stream()
            .filter(t -> t.getEstado() == EstadoTraslado.ENTREGADO || t.getEstado() == EstadoTraslado.COMPLETADO)
            .filter(t -> desdeDt == null || (t.getFechaCreacion() != null && !t.getFechaCreacion().isBefore(desdeDt)))
            .filter(t -> hastaDt == null || (t.getFechaCreacion() != null && !t.getFechaCreacion().isAfter(hastaDt)))
            .toList();

        // Agrupa por LER: total cantidad, conteo de traslados, gestores unicos
        var porLer = traslados.stream()
            .filter(t -> t.getResiduo() != null)
            .collect(Collectors.groupingBy(
                t -> t.getResiduo().getCodigoLER() == null ? "(sin LER)" : t.getResiduo().getCodigoLER()));

        List<Map<String, Object>> filas = porLer.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                String ler = entry.getKey();
                List<Traslado> grupo = entry.getValue();
                double cantidadTotal = grupo.stream()
                    .mapToDouble(t -> t.getResiduo() != null ? t.getResiduo().getCantidad() : 0).sum();
                String unidad = grupo.stream()
                    .filter(t -> t.getResiduo() != null && t.getResiduo().getUnidad() != null)
                    .map(t -> t.getResiduo().getUnidad()).findFirst().orElse("");
                String descripcion = grupo.stream()
                    .filter(t -> t.getResiduo() != null && t.getResiduo().getDescripcion() != null)
                    .map(t -> t.getResiduo().getDescripcion()).findFirst().orElse("");
                long gestoresUnicos = grupo.stream()
                    .map(t -> nombreCentro(t.getCentroGestor()))
                    .distinct().count();
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("codigoLER", ler);
                m.put("descripcion", descripcion);
                m.put("cantidadTotal", cantidadTotal);
                m.put("unidad", unidad);
                m.put("trasladosCompletados", grupo.size());
                m.put("gestoresUnicos", gestoresUnicos);
                return m;
            }).toList();

        List<Traslado> recogidas = recogidaRepo.findAll().stream()
            .filter(rc -> rc.getEstado() != null && rc.getEstado().name().equals("COMPLETADA"))
            .filter(rc -> desdeDt == null || (rc.getFechaProgramada() != null && !rc.getFechaProgramada().atStartOfDay().isBefore(desdeDt)))
            .filter(rc -> hastaDt == null || (rc.getFechaProgramada() != null && !rc.getFechaProgramada().atStartOfDay().isAfter(hastaDt)))
            .map(rc -> (Traslado) null) // solo para contar
            .toList();

        long recogidasCompletadas = recogidaRepo.findAll().stream()
            .filter(rc -> rc.getEstado() != null && rc.getEstado().name().equals("COMPLETADA"))
            .filter(rc -> desdeDt == null || (rc.getFechaProgramada() != null && !rc.getFechaProgramada().atStartOfDay().isBefore(desdeDt)))
            .filter(rc -> hastaDt == null || (rc.getFechaProgramada() != null && !rc.getFechaProgramada().atStartOfDay().isAfter(hastaDt)))
            .count();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("periodo", Map.of("desde", desde == null ? "(todo)" : desde,
                                    "hasta", hasta == null ? "(todo)" : hasta));
        body.put("resumen", Map.of(
            "trasladosCompletados", traslados.size(),
            "recogidasCompletadas", recogidasCompletadas,
            "codigosLerDistintos", porLer.size()
        ));
        body.put("porLer", filas);
        return ResponseEntity.ok(body);
    }

    // ─── Informe Final de Gestion — PDF (20.9) ───────────────────────────────

    @GetMapping("/final-gestion/pdf")
    @SuppressWarnings("unchecked")
    public ResponseEntity<byte[]> finalGestionPdf(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {

        // Reutiliza la logica del endpoint JSON
        ResponseEntity<Map<String, Object>> jsonResp = finalGestion(desde, hasta);
        Map<String, Object> data = jsonResp.getBody();

        List<Map<String, Object>> filas = (List<Map<String, Object>>) data.get("porLer");
        Map<String, Object> resumen = (Map<String, Object>) data.get("resumen");
        Map<String, Object> periodo = (Map<String, Object>) data.get("periodo");
        String periodoStr = periodo.get("desde") + " a " + periodo.get("hasta");

        byte[] pdf = pdfService.generarInformeFinalGestion(filas, resumen, periodoStr);

        String filename = "informe-final-gestion-" + LocalDate.now() + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ─── Checklist auditoria (20.7) ───────────────────────────────────────────

    /**
     * Para cada traslado del periodo: comprueba DI cerrado, NP vigente, contrato activo.
     * Devuelve semaforo verde/amarillo/rojo.
     */
    @GetMapping("/checklist-auditoria")
    public ResponseEntity<Map<String, Object>> checklistAuditoria(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {

        LocalDateTime desdeDt = parseFechaInicio(desde);
        LocalDateTime hastaDt = parseFechaFin(hasta);
        LocalDate hoy = LocalDate.now();

        List<Traslado> traslados = trasladoRepo.findAll().stream()
            .filter(t -> desdeDt == null || (t.getFechaCreacion() != null && !t.getFechaCreacion().isBefore(desdeDt)))
            .filter(t -> hastaDt == null || (t.getFechaCreacion() != null && !t.getFechaCreacion().isAfter(hastaDt)))
            .sorted(Comparator.comparing(Traslado::getFechaCreacion, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();

        List<Documento> todosDoc = documentoRepo.findAll();

        List<Map<String, Object>> items = traslados.stream().map(t -> {
            long trasladoId = t.getId();

            // ¿Tiene DI cerrado?
            boolean tieneDocumento = todosDoc.stream().anyMatch(d ->
                d.getTraslado() != null && d.getTraslado().getId() == trasladoId
                && d.getTipo() == TipoDocumento.DI
                && d.getEstado() != null && d.getEstado().name().equals("CERRADO"));

            // ¿Tiene NP vigente (no vencida)?
            boolean tieneNP = todosDoc.stream().anyMatch(d ->
                d.getTraslado() != null && d.getTraslado().getId() == trasladoId
                && d.getTipo() == TipoDocumento.NP
                && (d.getFechaVencimiento() == null || !d.getFechaVencimiento().isBefore(hoy)));

            // ¿Tiene contrato activo (del centro productor)?
            boolean tieneContrato = todosDoc.stream().anyMatch(d ->
                d.getCentro() != null && t.getCentroProductor() != null
                && d.getCentro().getId().equals(t.getCentroProductor().getId())
                && d.getTipo() == TipoDocumento.CONTRATO
                && (d.getFechaVencimiento() == null || !d.getFechaVencimiento().isBefore(hoy)));

            // Calcula semaforo: VERDE=todo ok, AMARILLO=alguno falta, ROJO=multiples faltan
            int faltan = (tieneDocumento ? 0 : 1) + (tieneNP ? 0 : 1) + (tieneContrato ? 0 : 1);
            String semaforo = faltan == 0 ? "VERDE" : (faltan == 1 ? "AMARILLO" : "ROJO");

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("trasladoId", t.getId());
            m.put("traslado", nullSafe(t.getCodigo() != null ? t.getCodigo() : t.getId()));
            m.put("estado", nullSafe(t.getEstado()));
            m.put("centroProductor", nombreCentro(t.getCentroProductor()));
            m.put("fechaCreacion", formatDateTime(t.getFechaCreacion()));
            m.put("tieneDocumento", tieneDocumento);
            m.put("tieneNP", tieneNP);
            m.put("tieneContrato", tieneContrato);
            m.put("semaforo", semaforo);
            return m;
        }).toList();

        long verdes    = items.stream().filter(i -> "VERDE".equals(i.get("semaforo"))).count();
        long amarillos = items.stream().filter(i -> "AMARILLO".equals(i.get("semaforo"))).count();
        long rojos     = items.stream().filter(i -> "ROJO".equals(i.get("semaforo"))).count();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("resumen", Map.of("verdes", verdes, "amarillos", amarillos, "rojos", rojos));
        body.put("items", items);
        return ResponseEntity.ok(body);
    }

    /** Acepta tanto los enums como string para validar en el cliente. */
    @GetMapping("/meta")
    public Map<String, Object> meta() {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("estadosTraslado", List.of(EstadoTraslado.values()).stream().map(Enum::name).toList());
        meta.put("tiposDocumento", List.of(TipoDocumento.values()).stream().map(Enum::name).toList());
        return meta;
    }

    private static LocalDateTime parseFechaInicio(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDate.parse(s).atStartOfDay();
    }

    private static LocalDateTime parseFechaFin(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDate.parse(s).atTime(23, 59, 59);
    }

    private static String formatDateTime(LocalDateTime dt) {
        return dt == null ? "" : dt.format(FECHA_HORA);
    }

    private static Object nullSafe(Object o) {
        return o == null ? "" : o;
    }

    private static String nombreCentro(Centro c) {
        return c == null ? "" : (c.getNombre() == null ? "" : c.getNombre());
    }

    private static void writeCsvLine(Writer w, List<?> values) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(';');
            sb.append(escapeCsv(stringify(values.get(i))));
        }
        sb.append("\r\n");
        w.write(sb.toString());
    }

    private static String stringify(Object v) {
        if (v == null) return "";
        if (v instanceof Number n) return n.toString();
        return v.toString();
    }

    /** Escapa segun RFC 4180 con separador ';' (mas amigable para Excel ES). */
    private static String escapeCsv(String s) {
        if (s == null) return "";
        boolean needsQuote = s.contains(";") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String escaped = s.replace("\"", "\"\"");
        return needsQuote ? "\"" + escaped + "\"" : escaped;
    }

    /** Empaqueta cabeceras + datos + mapper de filas. */
    private record ReportSpec(List<String> headers, List<Object> rows, Function<Object, List<Object>> mapper) {
        List<Object> toRow(Object item) { return new ArrayList<>(mapper.apply(item)); }
    }
}
