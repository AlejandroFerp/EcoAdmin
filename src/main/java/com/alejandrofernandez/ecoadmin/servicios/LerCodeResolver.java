package com.alejandrofernandez.ecoadmin.servicios;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alejandrofernandez.ecoadmin.modelo.ListaLer;
import com.alejandrofernandez.ecoadmin.repository.ListaLerRepository;

@Service
public class LerCodeResolver {

    private static final Map<String, String> LEGACY_DESCRIPTION_TO_CODE = Map.ofEntries(
            Map.entry("mezclas de residuos municipales", "200301"),
            Map.entry("envases de papel y carton", "150101"),
            Map.entry("residuos industria del cuero", "040199"),
            Map.entry("disolventes y mezclas organicas", "140603"),
            Map.entry("residuos de fibras textiles", "040222"),
            Map.entry("envases de plastico", "150102"),
            Map.entry("lodos del tratamiento de efluentes", "020204"),
            Map.entry("envases metalicos", "150104"),
            Map.entry("residuos sanitarios infecciosos", "180103"),
            Map.entry("productos quimicos de laboratorio", "180106"),
            Map.entry("residuos de plastico", "070213"),
            Map.entry("residuos de pintura y barniz", "080111"),
            Map.entry("materias no aptas para consumo", "020304"),
            Map.entry("envases mezclados", "150106"),
            Map.entry("residuos biodegradables de cocina", "200108"),
            Map.entry("virutas y torneados plasticos", "120105"),
            Map.entry("residuos de materiales compuestos textiles", "040209"),
            Map.entry("restos anatomicos y organos", "180102"),
            Map.entry("madera de embalajes", "170201"),
            Map.entry("materias no aptas para consumo lacteos", "020501"),
            Map.entry("heces orina y estiercol animal", "020106"),
            Map.entry("tubos fluorescentes con mercurio", "200121"),
            Map.entry("hierro y acero", "170405"));

    private final ListaLerRepository listaLerRepository;

    public LerCodeResolver(ListaLerRepository listaLerRepository) {
        this.listaLerRepository = listaLerRepository;
    }

    public String requireCanonicalCode(String rawCode) {
        if (!StringUtils.hasText(rawCode)) {
            throw new IllegalArgumentException("El codigo LER es obligatorio.");
        }

        return resolveCanonicalCode(rawCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El codigo LER '" + rawCode.trim() + "' no existe en la lista LER."));
    }

    public Optional<String> resolveCanonicalCode(String rawCode) {
        if (!StringUtils.hasText(rawCode)) {
            return Optional.empty();
        }

        return listaLerRepository.findByCodigoNormalizado(normalizeCode(rawCode))
                .map(ListaLer::getCodigo);
    }

    public Optional<String> resolveCanonicalCode(String rawCode, String legacyDescription) {
        Optional<String> byCode = resolveCanonicalCode(rawCode);
        if (byCode.isPresent()) {
            return byCode;
        }
        if (!StringUtils.hasText(legacyDescription)) {
            return Optional.empty();
        }

        String normalizedDescription = normalizeText(legacyDescription);
        String aliasedCode = LEGACY_DESCRIPTION_TO_CODE.get(normalizedDescription);
        if (aliasedCode != null) {
            Optional<String> byAlias = resolveCanonicalCode(aliasedCode);
            if (byAlias.isPresent()) {
                return byAlias;
            }
        }

        List<ListaLer> matches = listaLerRepository.findAllByDescripcionIgnoreCase(legacyDescription.trim());
        if (matches.size() == 1) {
            return Optional.of(matches.get(0).getCodigo());
        }

        List<ListaLer> normalizedMatches = listaLerRepository.findAll().stream()
                .filter(item -> normalizeText(item.getDescripcion()).equals(normalizedDescription))
                .toList();
        return normalizedMatches.size() == 1 ? Optional.of(normalizedMatches.get(0).getCodigo()) : Optional.empty();
    }

    private String normalizeCode(String rawCode) {
        return rawCode.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String rawText) {
        String withoutAccents = Normalizer.normalize(rawText, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return withoutAccents
                .replaceAll("[^\\p{Alnum}]+", " ")
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}