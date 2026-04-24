package com.iesdoctorbalmis.spring;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.iesdoctorbalmis.spring.modelo.Empresa;
import com.iesdoctorbalmis.spring.repository.EmpresaRepository;
import com.iesdoctorbalmis.spring.servicios.EmpresaService;

@SpringBootTest
@Transactional
class EmpresaServiceTest {

    @Autowired private EmpresaService empresaService;
    @Autowired private EmpresaRepository empresaRepo;

    @Test
    @DisplayName("obtener crea empresa por defecto si la tabla esta vacia")
    void obtener_tablaVacia_creaEmpresa() {
        empresaRepo.deleteAll();
        Empresa e = empresaService.obtener();
        assertNotNull(e);
        assertNotNull(e.getId());
        assertEquals("Mi Empresa", e.getNombre());
    }

    @Test
    @DisplayName("obtener devuelve la misma empresa en llamadas sucesivas")
    void obtener_dobleInvocacion_mismoId() {
        Empresa e1 = empresaService.obtener();
        Empresa e2 = empresaService.obtener();
        assertEquals(e1.getId(), e2.getId());
    }

    @Test
    @DisplayName("guardar actualiza los campos de la empresa existente")
    void guardar_actualizaCampos() {
        Empresa datos = new Empresa();
        datos.setNombre("EcoAdmin SL");
        datos.setCif("B12345678");
        datos.setNima("NIMA-001");
        datos.setTelefono("965123456");
        datos.setEmail("info@ecoadmin.com");

        Empresa saved = empresaService.guardar(datos);
        assertEquals("EcoAdmin SL", saved.getNombre());
        assertEquals("B12345678", saved.getCif());
        assertEquals("NIMA-001", saved.getNima());
        assertEquals("965123456", saved.getTelefono());
    }

    @Test
    @DisplayName("guardar con nombre vacio conserva el nombre anterior")
    void guardar_nombreVacio_conserva() {
        Empresa datosInicial = new Empresa();
        datosInicial.setNombre("Original SL");
        empresaService.guardar(datosInicial);

        Empresa datosNuevo = new Empresa();
        datosNuevo.setNombre("  ");
        datosNuevo.setCif("X99");

        Empresa result = empresaService.guardar(datosNuevo);
        assertEquals("Original SL", result.getNombre());
        assertEquals("X99", result.getCif());
    }
}
