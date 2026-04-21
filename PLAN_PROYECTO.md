# EcoAdmin — Plan de Proyecto Intermodular DAM

## Descripción del proyecto
Plataforma de gestión de traslados de residuos peligrosos (baterías de litio).
Garantiza trazabilidad completa desde generación hasta tratamiento final, con generación automática de documentos legales.

**Stack confirmado:** Spring Boot 4.0.0 + SQLite + Thymeleaf/Bootstrap 5 (aplicación web, sin cliente desktop)

---

## Estado actual del backend (ServidorApiRest)

### Lo que existe y funciona
- Entidades + CRUD completo: `Centro`, `Residuo`, `Usuario`
- Capa de servicio con interfaz + implementación para los 3 modelos
- `UsuarioDTO` (record) para no exponer password en la API
- `ConfiguracionAuditoria` → `@EnableJpaAuditing` con `AuditorAware`
- `SeguridadConfig` → Spring Security con `AuthenticationManager`, login en `/public/login`, redirect a `/public/index`
- `ZonaPublicaController` → rutas `/public/index` y `/public/login`
- `IndexController` → redirige `/` a `/public/index`

### Problemas pendientes de resolver
- `@CrossOrigin("*")` en todos los controllers → sustituir por config centralizada con CORS
- `Usuario.password` sin cifrar → añadir `BCryptPasswordEncoder`
- No existe tabla `Traslado` ni `Direccion`
- No existe autenticación basada en BD (UserDetailsService)
- No hay Swagger/OpenAPI
- No hay generación de PDF
- No hay envío de correo
- No hay QR
- No hay gráficos en el panel
- No existe ningún template `login.html`
- No existe frontend C# WPF

---

## Fases del proyecto

### FASE 1 — Base sólida de seguridad y BD (CRÍTICA)
- [x] 1.1 Crear `login.html` en templates
- [x] 1.2 `UserDetailsService` que cargue usuario desde BD
- [x] 1.3 `BCryptPasswordEncoder` para cifrar passwords
- [x] 1.4 Configurar CORS centralizado en `SeguridadConfig` (quitar `@CrossOrigin("*")`)
- [x] 1.5 Crear tabla `Direccion` (entidad + CRUD)
- [x] 1.6 Crear tabla `Traslado` (entidad + CRUD)
- [x] 1.7 Definir roles: `PRODUCTOR`, `GESTOR`, `TRANSPORTISTA`
- [x] 1.8 Añadir campo `rol` a `Usuario`

### FASE 2 — Modelo de datos completo (EcoAdmin)
- [x] 2.1 Entidad `Direccion` (calle, ciudad, CP, provincia, país)
- [x] 2.2 Entidad `Traslado` (origen, destino, residuo, transportista, estado, fechas)
- [x] 2.3 Enum `EstadoTraslado`: PENDIENTE, EN_TRANSITO, ENTREGADO, COMPLETADO
- [x] 2.4 Enum `Rol`: PRODUCTOR, GESTOR, TRANSPORTISTA, ADMIN
- [x] 2.5 Relaciones: `Traslado` → `Centro` (productor y gestor), `Traslado` → `Usuario` (transportista), `Traslado` → `Residuo`
- [x] 2.6 Añadir campo `codigoLER` a `Residuo`
- [x] 2.7 Historial de cambios de estado de traslado (entidad `EventoTraslado`)

### FASE 3 — API REST + Swagger
- [x] 3.1 Añadir dependencia SpringDoc OpenAPI (Swagger UI)
- [x] 3.2 `SwaggerConfig` con info del proyecto
- [x] 3.3 Controller `TrasladoController` con endpoints CRUD + cambio de estado + historial
- [x] 3.4 Controller `DireccionController`
- [x] 3.5 Endpoint de historial por traslado (`GET /api/traslados/{id}/historial`)
- [x] 3.6 Endpoint de filtro por estado (`GET /api/traslados/por-estado/{estado}`)
- [ ] 3.7 Anotar controllers con `@Operation` y `@Tag` (Swagger descriptors)

### FASE 4 — Generación de documentos (PDF)
- [ ] 4.1 Añadir dependencia iText o OpenPDF
- [ ] 4.2 Servicio `PdfService` con métodos:
  - `generarCartaDePorte(Traslado)`
  - `generarContratoTratamiento(Traslado)`
  - `generarNotificacionTraslado(Traslado)`
  - `generarCertificadoRecepcion(Traslado)`
- [ ] 4.3 Endpoint `/api/traslados/{id}/pdf/{tipo}` que devuelve el PDF

### FASE 5 — Email
- [ ] 5.1 Añadir dependencia `spring-boot-starter-mail`
- [ ] 5.2 Servicio `EmailService`
- [ ] 5.3 Envío al crear traslado, al cambiar estado y al generar certificado

### FASE 6 — Códigos QR
- [ ] 6.1 Añadir dependencia ZXing
- [ ] 6.2 Servicio `QrService` que genera QR por traslado
- [ ] 6.3 Endpoint `/api/traslados/{id}/qr`

### FASE 7 — Panel de control y gráficos (Thymeleaf)
- [ ] 7.1 Template `dashboard.html` con Bootstrap 5
- [ ] 7.2 Estadísticas: residuos generados, traslados por estado, alertas
- [ ] 7.3 Integrar Chart.js para gráficos de barras y circulares
- [ ] 7.4 Endpoint `/api/estadisticas` que devuelve datos agregados

~~### FASE 8 — Frontend C# WPF~~ ❌ Descartado

---

## Estructura de paquetes objetivo (backend)

```
com.iesdoctorbalmis.spring
├── config/
│   ├── ConfiguracionAuditoria.java   ✅
│   ├── SeguridadConfig.java          ✅ (CORS centralizado)
│   ├── SwaggerConfig.java            ✅
│   └── DataInitializer.java          ✅
├── controladores/
│   ├── IndexController.java          ✅
│   ├── ZonaPublicaController.java    ✅ (con estadísticas al modelo)
│   ├── UsuarioController.java        ✅
│   ├── CentroController.java         ✅
│   ├── ResiduoController.java        ✅
│   ├── TrasladoController.java       ✅ (CRUD + estado + historial + PDF + QR)
│   ├── DireccionController.java      ✅
│   └── EstadisticasController.java   ✅
├── dto/
│   ├── UsuarioDTO.java               ✅
│   ├── TrasladoDTO.java              ⬜
│   └── EstadisticasDTO.java          ⬜
├── modelo/
│   ├── Usuario.java                  ✅ (con fechaAlta, rol)
│   ├── Centro.java                   ✅
│   ├── Residuo.java                  ✅ (con codigoLER)
│   ├── Traslado.java                 ✅
│   ├── Direccion.java                ✅
│   ├── EventoTraslado.java           ✅
│   ├── enums/Rol.java                ✅
│   └── enums/EstadoTraslado.java     ✅
├── repository/
│   ├── CentroRepository.java         ✅
│   ├── ResiduoRepository.java        ✅
│   ├── UsuarioRepository.java        ✅
│   ├── TrasladoRepository.java       ✅
│   ├── DireccionRepository.java      ✅
│   └── EventoTrasladoRepository.java ✅
├── servicios/
│   ├── CentroService/DB              ✅
│   ├── ResiduoService/DB             ✅
│   ├── UsuarioService/DB             ✅
│   ├── TrasladoService/DB            ✅
│   ├── DireccionService/DB           ✅
│   ├── PdfService.java               ✅
│   ├── EmailService.java             ✅
│   └── QrService.java                ✅
└── Application.java                  ✅
```

---

## Orden de trabajo recomendado

1. **Fase 1** → sin seguridad correcta todo lo demás es frágil
2. **Fase 2** → el modelo de datos es la base de todo
3. **Fase 3** → la API es lo que consume el WPF
4. **Fase 4** → los PDF son requisito explícito del profesor
5. **Fase 7** → el dashboard es lo más visual para la presentación
6. **Fases 5 y 6** → email y QR son extras que suman nota
~~7. **Fase 8** → descartado~~

---

## Notas técnicas

- Spring Boot 4.0.0 requiere Java 21+
- SQLite: driver `org.xerial:sqlite-jdbc` + dialecto `hibernate-community-dialects` (SQLiteDialect)
- Fichero BD: `ecoadmin.db` generado en el directorio de trabajo; `ddl-auto=update`
- `@CrossOrigin("*")` debe eliminarse; centralizar CORS en `SeguridadConfig`
- Bootstrap 5 en pom.xml (5.3.3 vía webjars)
- iText 7 o OpenPDF para PDF (OpenPDF es LGPL, más libre)
- ZXing para QR
- SpringDoc 2.x para Swagger (compatible con Spring Boot 3+/4)
