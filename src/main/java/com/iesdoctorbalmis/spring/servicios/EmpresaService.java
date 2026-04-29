package com.iesdoctorbalmis.spring.servicios;

import org.springframework.stereotype.Service;

import com.iesdoctorbalmis.spring.modelo.Empresa;
import com.iesdoctorbalmis.spring.repository.EmpresaRepository;

/**
 * Singleton de empresa: siempre devuelve el primer registro existente.
 * Si la tabla esta vacia, lo crea vacio en la primera lectura.
 */
@Service
public class EmpresaService {

    private final EmpresaRepository repo;

    public EmpresaService(EmpresaRepository repo) {
        this.repo = repo;
    }

    public Empresa obtener() {
        return repo.findAll().stream().findFirst().orElseGet(() -> {
            Empresa e = new Empresa();
            e.setNombre("Mi Empresa");
            return repo.save(e);
        });
    }

    public Empresa guardar(Empresa nuevo) {
        Empresa actual = obtener();
        actual.setNombre(nullSafe(nuevo.getNombre(), actual.getNombre()));
        actual.setCif(nuevo.getCif());
        actual.setNima(nuevo.getNima());
        actual.setTelefono(nuevo.getTelefono());
        actual.setEmail(nuevo.getEmail());
        actual.setWeb(nuevo.getWeb());
        actual.setDireccion(nuevo.getDireccion());
        actual.setCiudad(nuevo.getCiudad());
        actual.setCodigoPostal(nuevo.getCodigoPostal());
        actual.setProvincia(nuevo.getProvincia());
        actual.setPais(nuevo.getPais());
        actual.setAutorizacionGestor(nuevo.getAutorizacionGestor());
        actual.setAutorizacionTransportista(nuevo.getAutorizacionTransportista());
        actual.setAutorizacionProductor(nuevo.getAutorizacionProductor());
        actual.setLogoUrl(nuevo.getLogoUrl());
        actual.setObservaciones(nuevo.getObservaciones());
        actual.normalizarDireccionFiscal();
        return repo.save(actual);
    }

    private static String nullSafe(String v, String fallback) {
        return (v == null || v.isBlank()) ? fallback : v.trim();
    }
}
