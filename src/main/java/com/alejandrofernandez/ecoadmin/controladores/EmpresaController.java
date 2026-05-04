package com.alejandrofernandez.ecoadmin.controladores;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alejandrofernandez.ecoadmin.modelo.Empresa;
import com.alejandrofernandez.ecoadmin.servicios.EmpresaService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Empresa", description = "Datos legales de la empresa (singleton)")
@RestController
@RequestMapping("/api/empresa")
public class EmpresaController {

    private final EmpresaService service;

    public EmpresaController(EmpresaService service) {
        this.service = service;
    }

    /** Cualquier usuario autenticado puede leer los datos de la empresa. */
    @GetMapping
    public ResponseEntity<Empresa> obtener() {
        return ResponseEntity.ok(service.obtener());
    }

    /** Solo ADMIN puede modificar los datos legales de la empresa. */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Empresa> guardar(@RequestBody Empresa empresa) {
        return ResponseEntity.ok(service.guardar(empresa));
    }
}
