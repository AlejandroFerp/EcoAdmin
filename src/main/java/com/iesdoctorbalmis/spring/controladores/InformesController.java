package com.iesdoctorbalmis.spring.controladores;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
import com.iesdoctorbalmis.spring.modelo.Residuo;
import com.iesdoctorbalmis.spring.modelo.Traslado;
import com.iesdoctorbalmis.spring.modelo.enums.EstadoTraslado;
import com.iesdoctorbalmis.spring.modelo.enums.TipoDocumento;
import com.iesdoctorbalmis.spring.repository.CentroRepository;
import com.iesdoctorbalmis.spring.repository.DocumentoRepository;
import com.iesdoctorbalmis.spring.repository.ResiduoRepository;
import com.iesdoctorbalmis.spring.repository.TrasladoRepository;

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

    public InformesController(TrasladoRepository trasladoRepo,
                              ResiduoRepository residuoRepo,
                              CentroRepository centroRepo,
                              DocumentoRepository documentoRepo) {
        this.trasladoRepo = trasladoRepo;
        this.residuoRepo = residuoRepo;
        this.centroRepo = centroRepo;
        this.documentoRepo = documentoRepo;
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

            default -> throw new IllegalArgumentException("Tipo de informe desconocido: " + tipo
                + ". Valores validos: traslados, residuos, centros, documentos.");
        };
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

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
