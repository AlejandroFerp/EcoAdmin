package com.iesdoctorbalmis.spring.servicios;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iesdoctorbalmis.spring.dto.RutaTransportistaInputDTO;
import com.iesdoctorbalmis.spring.dto.RutaTransportistaViewDTO;
import com.iesdoctorbalmis.spring.excepciones.RecursoNoEncontradoException;
import com.iesdoctorbalmis.spring.modelo.Ruta;
import com.iesdoctorbalmis.spring.modelo.RutaTransportista;
import com.iesdoctorbalmis.spring.modelo.Usuario;
import com.iesdoctorbalmis.spring.repository.RutaRepository;
import com.iesdoctorbalmis.spring.repository.RutaTransportistaRepository;
import com.iesdoctorbalmis.spring.repository.UsuarioRepository;

@Service
public class RutaTransportistaService {

    private final RutaTransportistaRepository rtRepo;
    private final RutaRepository rutaRepo;
    private final UsuarioRepository usuarioRepo;
    private final TarifaValidator tarifaValidator;

    public RutaTransportistaService(RutaTransportistaRepository rtRepo,
            RutaRepository rutaRepo,
            UsuarioRepository usuarioRepo,
            TarifaValidator tarifaValidator) {
        this.rtRepo = rtRepo;
        this.rutaRepo = rutaRepo;
        this.usuarioRepo = usuarioRepo;
        this.tarifaValidator = tarifaValidator;
    }

    /**
     * Lista los transportistas activos de una ruta, enriquecidos con la fórmula
     * efectiva
     * y un precio de ejemplo para w=100 kg.
     */
    public List<RutaTransportistaViewDTO> listarConPrecio(Long rutaId) {
        Ruta ruta = rutaRepo.findById(rutaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada: " + rutaId));
        return rtRepo.findByRutaIdAndActivoTrue(rutaId).stream()
                .map(rt -> toViewDTO(rt, ruta))
                .collect(Collectors.toList());
    }

    /**
     * Rutas en las que un transportista está activamente asignado (para la vista de
     * lista).
     */
    public List<Ruta> getRutasPorTransportista(Long transId) {
        return rtRepo.findRutasByTransportista(transId);
    }

    /**
     * Asigna un transportista a una ruta con su fórmula propia.
     * Si ya existía una asignación (incluso inactiva), la reactiva y actualiza.
     */
    @Transactional
    public RutaTransportista asignar(Long rutaId, RutaTransportistaInputDTO dto) {
        Ruta ruta = rutaRepo.findById(rutaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada: " + rutaId));
        Usuario trans = usuarioRepo.findById(dto.transportistaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Transportista no encontrado: " + dto.transportistaId()));
        RutaTransportista rt = rtRepo.findByRutaIdAndTransportistaId(rutaId, dto.transportistaId())
                .orElse(new RutaTransportista());
        rt.setRuta(ruta);
        rt.setTransportista(trans);
        rt.setActivo(true);
        if (dto.formulaTarifa() != null && !dto.formulaTarifa().isBlank()) {
            TarifaValidator.ResultadoValidacion rv = tarifaValidator.validar(dto.formulaTarifa());
            if (!rv.valido())
                throw new IllegalArgumentException(rv.mensaje());
            rt.setFormulaTarifa(dto.formulaTarifa().trim());
        } else if (dto.formulaTarifa() != null) {
            rt.setFormulaTarifa(null);
        }
        if (dto.unidadTarifa() != null)
            rt.setUnidadTarifa(dto.unidadTarifa().isBlank() ? null : dto.unidadTarifa().trim());
        return rtRepo.save(rt);
    }

    /** Actualiza la fórmula/moneda del transportista en esta ruta. */
    @Transactional
    public RutaTransportista actualizar(Long rutaId, Long transId, RutaTransportistaInputDTO dto) {
        RutaTransportista rt = rtRepo.findByRutaIdAndTransportistaId(rutaId, transId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asignación no encontrada"));
        // Null en el DTO = limpiar el override (heredar la fórmula de la ruta)
        rt.setFormulaTarifa(
                dto.formulaTarifa() == null || dto.formulaTarifa().isBlank() ? null : dto.formulaTarifa().trim());
        rt.setUnidadTarifa(
                dto.unidadTarifa() == null || dto.unidadTarifa().isBlank() ? null : dto.unidadTarifa().trim());
        return rtRepo.save(rt);
    }

    /** Elimina la asignación de un transportista a una ruta. */
    @Transactional
    public void desasignar(Long rutaId, Long transId) {
        int borrados = rtRepo.eliminarAsignacion(rutaId, transId);
        if (borrados == 0) {
            throw new RecursoNoEncontradoException("Asignación no encontrada");
        }
    }

    /**
     * Calcula el precio para un transportista en una ruta dado el peso del residuo.
     * Usa la fórmula propia del transportista; si no tiene, cae al fallback de la
     * ruta.
     */
    public Map<String, Object> calcularPrecio(Long rutaId, Long transId, double w) {
        return calcularPrecio(rutaId, transId, w, null);
    }

    public Map<String, Object> calcularPrecio(Long rutaId, Long transId, double w, Double distanciaKm) {
        return calcularPrecio(rutaId, transId, w, distanciaKm, null);
    }

    public Map<String, Object> calcularPrecio(Long rutaId, Long transId, double w, Double distanciaKm,
            String formulaTemporal) {
        Ruta ruta = rutaRepo.findById(rutaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada: " + rutaId));
        RutaTransportista rt = rtRepo.findByRutaIdAndTransportistaId(rutaId, transId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asignación no encontrada"));

        String formulaPropia = rt.getFormulaTarifa();
        String formula = formulaTemporal != null
            ? formulaTemporal
            : (formulaPropia != null && !formulaPropia.isBlank())
                        ? formulaPropia
                        : ruta.getFormulaTarifa();
        String moneda = (rt.getUnidadTarifa() != null && !rt.getUnidadTarifa().isBlank())
                ? rt.getUnidadTarifa()
                : (ruta.getUnidadTarifa() != null ? ruta.getUnidadTarifa() : "EUR");

        if (formula == null || formula.isBlank()) {
            return Map.of("error", "Sin tarifa definida para esta ruta.");
        }
        TarifaValidator.ResultadoValidacion validacion = tarifaValidator.validar(formula);
        if (!validacion.valido()) {
            return Map.of("error", validacion.mensaje());
        }
        double L = distanciaKm != null ? distanciaKm : (ruta.getDistanciaKm() != null ? ruta.getDistanciaKm() : 0.0);
        try {
            double resultado = tarifaValidator.calcular(formula, w, L);
            if (!Double.isFinite(resultado)) {
                return Map.of("error", "La fórmula produce un resultado no finito.");
            }
            return Map.of(
                    "formula", formula,
                    "w", w,
                    "L", L,
                    "resultado", Math.round(resultado * 100.0) / 100.0,
                    "moneda", moneda,
                    "formulaPropia", formulaPropia != null && !formulaPropia.isBlank(),
                    "transportista", rt.getTransportista().getNombre());
        } catch (Exception e) {
            return Map.of("error", "Error en la fórmula: " + e.getMessage());
        }
    }

    /** Verifica si un transportista está activamente asignado a la ruta. */
    public boolean perteneceARuta(Long rutaId, Long transId) {
        return rtRepo.existsByRutaIdAndTransportistaIdAndActivoTrue(rutaId, transId);
    }

    // ——— helpers privados ———

    private RutaTransportistaViewDTO toViewDTO(RutaTransportista rt, Ruta ruta) {
        // Formula efectiva: propia del transportista si la tiene, sino la de la ruta
        String formulaPropia = rt.getFormulaTarifa();
        String formulaEfectiva = (formulaPropia != null && !formulaPropia.isBlank())
                ? formulaPropia
                : ruta.getFormulaTarifa();
        String moneda = (rt.getUnidadTarifa() != null && !rt.getUnidadTarifa().isBlank())
                ? rt.getUnidadTarifa()
                : ruta.getUnidadTarifa();
        Double precio = null;
        if (formulaEfectiva != null && !formulaEfectiva.isBlank()) {
            try {
                double L = ruta.getDistanciaKm() != null ? ruta.getDistanciaKm() : 0.0;
                double p = tarifaValidator.calcular(formulaEfectiva, 100.0, L);
                precio = Double.isFinite(p) ? Math.round(p * 100.0) / 100.0 : null;
            } catch (Exception ignored) {
            }
        }
        return new RutaTransportistaViewDTO(
                rt.getId(),
                rt.getTransportista().getId(),
                rt.getTransportista().getNombre(),
                rt.getTransportista().getEmail(),
                formulaPropia,
                formulaEfectiva,
                moneda,
                precio);
    }
}
