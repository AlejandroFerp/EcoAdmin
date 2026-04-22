# EcoAdmin — Plan de Proyecto Intermodular DAM

## Descripcion del proyecto
Plataforma de gestion de traslados de residuos peligrosos (baterias de litio).
Garantiza trazabilidad completa desde generacion hasta tratamiento final, con generacion automatica de documentos legales.

**Stack:** Spring Boot 4.0.5 / Java 25 + SQLite (dev) / H2 (test) / PostgreSQL (prod) + Thymeleaf + Tailwind CSS (CDN)
**Referencia:** [myshipment (PHP/Laravel)](https://github.com/AlejandroFerp/myshipment) para modelo de datos y acciones

---

## Fases completadas

### FASE 1 — Base solida de seguridad y BD [COMPLETADA]
- [x] login.html, UserDetailsService, BCryptPasswordEncoder
- [x] CORS centralizado, Direccion, Traslado, roles, campo rol en Usuario

### FASE 2 — Modelo de datos completo [COMPLETADA]
- [x] Entidades: Direccion, Traslado, EstadoTraslado (enum con maquina de estados)
- [x] Roles: PRODUCTOR, GESTOR, TRANSPORTISTA, ADMIN
- [x] Relaciones: Traslado -> Centro (productor/gestor), Traslado -> Usuario (transportista), Traslado -> Residuo
- [x] codigoLER en Residuo, EventoTraslado (historial de cambios)

### FASE 3 — API REST + Swagger [COMPLETADA]
- [x] SpringDoc OpenAPI, CRUD para todas las entidades
- [x] Endpoints de historial, filtro por estado, cambio de estado
- [x] Ownership-based access control por rol
- [ ] Anotar controllers con @Operation y @Tag

### FASE 9 — Revision de seguridad y calidad [COMPLETADA]
- [x] 17 issues de seguridad/logica/estructura corregidos
- [x] @JsonProperty(WRITE_ONLY) para password
- [x] Rutas /public/ corregidas en ZonaPublicaController
- [x] Verificacion compatibilidad thymeleaf-extras-springsecurity6 con Security 7
- [x] 40 tests (AccesoController, TrasladoService, TrasladoEndpoint, EstadoTraslado)

### FASE 10 — Modelo de datos v2 (alineacion con PHP) [COMPLETADA]
- [x] Centro.direccion: String -> @ManyToOne Direccion (FK real)
- [x] Centro: campos nuevos nima, telefono, email, nombreContacto, detalleEnvio
- [x] Direccion: campos nuevos nombre, descripcion, calle2, latitud, longitud
- [x] Residuo: campo nuevo descripcion
- [x] ListaLer: nueva entidad (catalogo europeo de residuos, 953 codigos)
- [x] ListaLerRepository + ListaLerController (/api/lista-ler con busqueda)
- [x] DataInitializer: seed completo (5 usuarios, 8 direcciones, 6 centros, 7 residuos, 7 traslados con historial)
- [x] DireccionController: acceso para todos los autenticados (no solo ADMIN)
- [x] CentroServiceDB: resuelve Direccion FK antes de guardar
- [x] Tests actualizados: todos 40 pasan

---

## Fases pendientes

### FASE 11 — Sistema de theming (dark mode + componentes) [EN PROGRESO]
- [x] 11.1 Tema oscuro azulado: fondo slate-900, paneles slate-800, acento blue-500
- [x] 11.2 Toggle dark/light en sidebar con persistencia en localStorage
- [x] 11.3 Sistema de clases CSS global (btn-primary, btn-danger, etc.)
- [x] 11.4 Iconos consistentes: editar=lapiz, borrar=papelera con tooltip, QR coherente
- [x] 11.5 Layout compartido: extraer sidebar/header/footer a Thymeleaf fragments

### FASE 12 — UX de tablas
- [x] 12.1 Doble clic para abrir detalle/edicion
- [x] 12.2 Seleccion batch + acciones masivas (eliminar, cambiar estado, exportar)
- [x] 12.3 Crear entidad desde dropdown (opcion "+ Crear nuevo" en selects)

### FASE 13 — Vistas separadas (crear vs detalle)
- [x] 13.1 Modal ligero para crear (campos obligatorios)
- [x] 13.2 Vista de detalle expandida con tabs (datos, documentos, historial, fotos)
- [x] 13.3 Timeline visual del historial de estados en traslados
- [ ] 13.4 Seccion de fotos/adjuntos en centros y traslados

### FASE 14 — Direcciones y mapa
- [x] 14.1 Vista de mapa con Leaflet.js (marcadores de centros y direcciones)
- [x] 14.2 Selector de direccion con mapa (clic para establecer lat/lon)
- [x] 14.3 Geocodificacion inversa con Nominatim/OpenStreetMap

### FASE 15 — Generacion de PDFs
- [x] 15.1 PdfService con OpenPDF: carta de porte, certificado recepcion, notificacion previa
- [x] 15.2 Endpoint GET /api/traslados/{id}/pdf/{tipo}
- [x] 15.3 Informe resumen desde dashboard (estadisticas por periodo)
- [ ] 15.4 Referencia de formatos: repo PHP myshipment

### FASE 16 — Email y QR (extras)
- [x] 16.1 Email en cambios de estado
- [ ] 16.2 QR mejorado con logo EcoAdmin
- [x] 16.3 Anotar controllers con @Operation y @Tag (Swagger)

---

### FASE 16.5 — Deuda tecnica frontend (consolidacion CSS/Tailwind) [PENDIENTE]

**Contexto:** el patron actual (Utility-First Tailwind + Component Extraction con clases `.ea-*`)
es correcto y profesional, pero la implementacion esta a nivel de prototipo. Esta fase consolida
la capa de estilos sin cambiar el patron, antes de entrar en produccion real.

**Estado actual a corregir:**
- Tailwind cargado por CDN (`cdn.tailwindcss.com`) — el propio Tailwind lo marca como "not for production".
- Tres librerias de componentes simultaneas (Tailwind + DaisyUI + Flowbite) con solapamientos.
- ~750 lineas de `<style>` inline en `templates/layouts/main.html`.
- Doble sistema sin integrar: tokens `--eco-*` en `app.css` + clases `.ea-*` en `styles.css` con colores hardcodeados (`#3b82f6` en vez de `var(--eco-primary)`).
- `styles.css` cargado solo desde `header.html`, no desde el `layout(...)` principal — paginas inconsistentes.
- Reglas globales sobre tags genericos (`table { border: 0; }`, `td { font-size: 16px; }`) afectan todo, incluso terceros.
- Comentarios "Phase 11.3" en codigo de produccion.

**Tareas:**
- [ ] 16.5.1 Instalar Tailwind como dependencia npm + build a un unico `app.css` minificado con purge/content scan (objetivo: pasar de ~3 MB a ~10 KB).
- [ ] 16.5.2 Configurar `tailwind.config.js` con los tokens `--eco-*` integrados como utilidades (`bg-eco-primary`, `text-eco-danger`, etc.).
- [ ] 16.5.3 Elegir UNA libreria de componentes (DaisyUI O Flowbite) y eliminar la otra.
- [ ] 16.5.4 Mover el `<style>` inline de `main.html` a `app.css` (separar en bloques: tipografia, scrollbars, form-control-eco, kanban, nav).
- [ ] 16.5.5 Unificar `styles.css` dentro de `app.css` y reemplazar colores hardcodeados por `var(--eco-*)` para que un cambio de token propague a todas las clases `.ea-*`.
- [ ] 16.5.6 Cargar `app.css` desde `layouts/main.html` UNICAMENTE (eliminar carga duplicada en `header.html`).
- [ ] 16.5.7 Eliminar reglas sobre tags globales (`table`, `td`, `.navbar`, `.jumbotron`, `footer`) o convertirlas en clases (`.ea-table`, `.ea-table-cell`).
- [ ] 16.5.8 Decidir politica para `login.html` y `preview.html` (paginas standalone): o usan el layout o documentar por que no.
- [ ] 16.5.9 Anadir lint de CSS (Stylelint con `stylelint-config-standard`) y un script `npm run css:build` integrado en el ciclo de Maven (frontend-maven-plugin) para que `mvn package` produzca el CSS final.
- [ ] 16.5.10 Eliminar comentarios "Phase 11.3" y similares del codigo de produccion.

**Criterio de finalizacion:** un solo `<link>` a `app.css` minificado en `main.html`, sin `<script src="cdn.tailwindcss.com">`, sin `<style>` inline, una unica libreria de componentes, tokens unificados.

**Patron objetivo (sin cambiar):** Utility-First con Component Extraction (estandar Tailwind, oficialmente recomendado para apps que crecen mas alla del prototipo).

---

## Orden de ejecucion

| Prioridad | Fase | Justificacion |
|-----------|------|---------------|
| 1 | 11 (theming) | Base visual para todo lo demas |
| 2 | 12 (UX tablas) | Mejora inmediata en experiencia |
| 3 | 13 (vistas) | Diferencia grande en calidad percibida |
| 4 | 14 (mapas) | Feature diferencial para presentacion |
| 5 | 15 (PDFs) | Requisito explicito del proyecto |
| 6 | 16 (extras) | Cierre y detalles finales |

### Dependencias
```
11 --> 12 --> 13
10 --> 14
15 (independiente, puede ir en paralelo desde fase 10)
```

---

## Estructura de paquetes

```
com.iesdoctorbalmis.spring
+-- config/
|   +-- ConfiguracionAuditoria    @EnableJpaAuditing
|   +-- SeguridadConfig           Spring Security 7, CORS, roles
|   +-- DataInitializer           Seed completo + catalogo LER
+-- controladores/
|   +-- IndexController           Redirect / -> /public/index
|   +-- ZonaPublicaController     Vistas Thymeleaf
|   +-- UsuarioController         API CRUD (solo ADMIN)
|   +-- CentroController          API CRUD + ownership
|   +-- ResiduoController         API CRUD + ownership
|   +-- TrasladoController        API CRUD + estado + historial
|   +-- DireccionController       API CRUD
|   +-- EstadisticasController    API agregados
|   +-- ListaLerController        API catalogo LER (busqueda)
|   +-- QrController              API generacion QR
+-- dto/
|   +-- UsuarioDTO                Record sin password
+-- modelo/
|   +-- Usuario                   id, nombre, email, password, rol, fechaAlta
|   +-- Centro                    id, usuario, nombre, tipo, direccion(FK), nima, telefono, email, nombreContacto
|   +-- Residuo                   id, cantidad, unidad, estado, codigoLER, descripcion, centro(FK)
|   +-- Direccion                 id, nombre, descripcion, calle, calle2, ciudad, codigoPostal, provincia, pais, lat, lon
|   +-- Traslado                  id, centroProductor, centroGestor, residuo, transportista, estado, fechas, observaciones
|   +-- EventoTraslado            id, traslado, estadoAnterior, estadoNuevo, fecha, comentario, usuario
|   +-- ListaLer                  id, codigo, descripcion (953 codigos europeos)
|   +-- enums/Rol                 PRODUCTOR, GESTOR, TRANSPORTISTA, ADMIN
 |   +-- enums/EstadoTraslado      Libertad total entre estados (rectificable, con historial)
+-- repository/                   JpaRepository para cada entidad
+-- servicios/                    Interface + DB impl para cada entidad
```

---

## Notas tecnicas
- Spring Boot 4.0.5 / Java 25 (JDK en `C:\Users\afp5\.jdk\jdk-25`)
- Spring Security 7.0.0, thymeleaf-extras-springsecurity6 (compatible)
- SQLite (dev), H2 (test), PostgreSQL (prod)
- @AutoConfigureMockMvc NO existe en SB4: usar MockMvcBuilders.webAppContextSetup()
- Frontend: Tailwind CSS (CDN) + FullCalendar 6.1.15 + HTMX
- OpenPDF, ZXing, spring-boot-starter-mail, springdoc-openapi 2.8.6
- Jackson 3.x (parte de SB4): serializa LocalDateTime como ISO-8601 por defecto (no como arrays)

---

## Observaciones: Bugs recurrentes y precauciones

### 1. Variable de entorno ADMIN_PASSWORD
**Bug:** El DataInitializer no crea datos de seed si `ADMIN_PASSWORD` esta vacia. Esto deja la BD completamente vacia y el login falla sin mensaje claro.
**Solucion:** Se agrego default `admin123` en application.properties: `ecoadmin.admin.password=${ADMIN_PASSWORD:admin123}`
**Precaucion:** En produccion, SIEMPRE definir ADMIN_PASSWORD como variable de entorno real.

### 2. Start-Process y JAVA_HOME / cwd en PowerShell
**Bug:** `Start-Process -FilePath ".\mvnw.cmd"` falla si el cwd del shell no es el directorio del proyecto. Tambien, Start-Process hereda env vars del proceso padre, pero NO si se usa `-Environment` (no existe en PS 5.1).
**Solucion:** Usar rutas absolutas en `-FilePath` y siempre `cd` al directorio correcto antes. Setear `$env:JAVA_HOME` y `$env:Path` ANTES de llamar Start-Process.
**Precaucion:** Compilar y ejecutar en el mismo shell donde se setearon las env vars. Verificar cwd con `pwd` si Start-Process da "archivo no encontrado".

### 3. UnsupportedClassVersionError (class 69 vs 65)
**Bug:** Compilar con JDK 25 (class file 69) y ejecutar con JDK 21 (class file 65) produce error al arrancar.
**Solucion:** Asegurar que JAVA_HOME apunta a JDK 25 tanto para compilacion como para ejecucion.
**Precaucion:** Start-Process hereda JAVA_HOME del proceso padre. Si se cambio PATH pero no JAVA_HOME en el shell hijo, puede usar otra version de Java.

### 4. Jackson 3.x: `write-dates-as-timestamps` no existe
**Bug:** En Spring Boot 4.x + Jackson 3.x, `spring.jackson.serialization.write-dates-as-timestamps=false` causa error fatal porque `SerializationFeature.WRITE_DATES_AS_TIMESTAMPS` no existe.
**Solucion:** Jackson 3.x serializa LocalDateTime como ISO-8601 string por defecto; no hace falta la propiedad. Si los datos llegan como arrays `[y,m,d,h,m,s]`, convertir en JS con un helper `toISO(val)`.
**Precaucion:** No copiar propiedades Jackson de proyectos Spring Boot 2.x/3.x sin verificar compatibilidad con SB4.

### 5. FullCalendar events callback signature
**Bug:** Si se pasa `events: miFuncion` donde `miFuncion` retorna un array, FullCalendar 6.x no lo llama correctamente porque espera `function(fetchInfo, successCallback, failureCallback)`.
**Solucion:** Wrappear: `events: function(fetchInfo, successCb, failureCb) { successCb(miArray()); }`
**Precaucion:** Siempre usar la forma con callbacks cuando la fuente de datos es local/sincrona.

### 6. Thymeleaf cache en desarrollo
**Bug:** Cambios en templates HTML no se reflejan sin reiniciar si el servidor uso la version compilada anterior.
**Solucion:** `spring.thymeleaf.cache=false` (ya configurado). Si persiste, recompilar con `mvnw compile`.
**Precaucion:** Verificar que el target/classes tiene la version actualizada del template.

### 7. Archivo login.html duplicado
**Bug:** Al recrear login.html, el contenido viejo se pego al nuevo resultando en 2 documentos HTML consecutivos en el mismo archivo. El navegador renderizo ambos forms.
**Solucion:** Verificar longitud del archivo despues de crear/editar. Truncar si tiene contenido sobrante.
**Precaucion:** Al reescribir un archivo completo, verificar que el resultado tiene exactamente 1 estructura HTML.

---

## BLOQUE II — Evolucion del producto (nuevas fases)

### Contexto: nueva estructura de navegacion

El usuario ha definido una nueva arquitectura de navegacion con 8 secciones de primer nivel.
La aplicacion crece de ser un gestor de traslados a una plataforma completa de gestion de residuos
con cumplimiento legal integrado (RD 553/2020, Ley 7/2022).

```
SIDEBAR
├── Principal
│   ├── Dashboard         (existente → mejorar)
│   ├── Recogidas         (traslados rebrandeados + workflow)
│   └── Rutas             (NUEVO: planificacion de transporte)
├── Almacen               (residuos + FIFO + calendario)
├── Contactos             (centros + personas de contacto)
├── Negocio               (datos empresa, NIMA, autorizaciones)
├── Mis Datos             (perfil de usuario autenticado)
├── Documentos            (DI, NP, Contratos, Archivo Cronologico)
└── Informes              (reporting legal + estadisticas)
```

---

### FASE 17 — Nueva navegacion y reorganizacion de modulos

**Objetivo:** Reestructurar el sidebar para reflejar la nueva arquitectura de 8 secciones.
No se elimina funcionalidad, se reorganiza y renombra.

**Tareas:**

- [x] 17.1 Sidebar con grupos colapsables: Principal (con subnivel), Almacen, Contactos, Negocio, Mis Datos, Documentos, Informes
- [x] 17.2 Rutas de Thymeleaf: `/rutas`, `/negocio`, `/mis-datos`
- [x] 17.3 Renombrar "Traslados" → "Recogidas" en toda la UI (backend mantiene nombre, solo vista cambia)
- [x] 17.4 Renombrar "Residuos" → "Almacen" en la navegacion
- [x] 17.5 Mover centros + usuarios bajo "Contactos" (centros = instalaciones, usuarios = personas)
- [x] 17.6 Crear pagina vacia con placeholder para Rutas, Negocio, Mis Datos
- [x] 17.7 Indicador de pagina activa en sidebar (clase `active` por ruta)
- [x] 17.8 Breadcrumb en cabecera: Principal > Recogidas > Detalle

#### Mejoras adicionales Fase 17 (implementadas)
- [x] 17.9 Vista Kanban de traslados (columnas por estado, drag visual)
- [x] 17.10 Vista Calendario de traslados (FullCalendar 6.1.15, integrada como 3ra pestaña)
       - Chips de filtro por estado (activar/desactivar)
       - Buscador global filtra las 3 vistas (lista, kanban, calendario)
       - Eventos coloreados por estado
- [x] 17.11 Campos `fechaProgramadaInicio` / `fechaProgramadaFin` en Traslado (para calendario)
- [x] 17.12 Campo `fechaUltimoCambioEstado` en Traslado (denormalizado, actualizado en cambiarEstado)
- [x] 17.13 Login rediseñado estilo MaterialDark (fondo gradiente verde ecologista, card glassmorphism, inputs Material con floating labels)
- [x] 17.14 Default `admin123` para ADMIN_PASSWORD en dev (evita seed vacio)

**Impacto en backend:** Minimo. Solo anadir endpoints de vista en ZonaPublicaController.

---

### FASE 18 — Calendario y seguimiento FIFO de almacen

**Objetivo:** Registrar el ciclo completo de un residuo en almacen: entrada, permanencia y salida.
Garantizar trazabilidad temporal y alerta de incumplimientos de plazo.

**Contexto legal:**
El RD 553/2020 y la Ley 7/2022 obligan a registrar las fechas de almacenamiento.
El Archivo Cronologico (obligatorio) debe incluir: fecha de almacenamiento y fecha de entrega al gestor.
Los residuos peligrosos no pueden superar ciertos tiempos de almacenamiento (tipicamente 6 meses para
productores, 1 ano para gestores). La gestion FIFO (primero en entrar, primero en salir) es la
practica correcta para evitar que residuos queden sin gestionar.

**Modelo de datos nuevo:**

```java
// Extension de Residuo (nuevos campos)
Residuo {
  // existentes...
  LocalDateTime fechaEntradaAlmacen;   // cuando llego al almacen
  LocalDateTime fechaSalidaAlmacen;    // cuando salio (null si sigue)
  Integer diasMaximoAlmacenamiento;    // umbral de alerta (default: 180)
}

// Nueva entidad: Recogida programada
Recogida {
  Long id;
  Residuo residuo;
  Centro centroOrigen;
  Centro centroDestino;
  Usuario transportista;
  LocalDate fechaProgramada;
  LocalDate fechaRealizada;
  EstadoRecogida estado;               // PROGRAMADA, EN_CURSO, COMPLETADA, CANCELADA
  String observaciones;
}
```

**Tareas:**

- [x] 18.1 Anadir `fechaEntradaAlmacen`, `fechaSalidaAlmacen`, `diasMaximoAlmacenamiento` a Residuo
- [x] 18.2 Crear entidad `Recogida` con su repositorio y servicio
- [x] 18.3 Vista de calendario (`/recogidas/calendario`) con FullCalendar.js (licencia MIT)
       - Recogidas programadas se muestran como eventos
       - Color por estado: azul=programada, amarillo=en_curso, verde=completada, rojo=cancelada
       - Click en evento abre detalle/edicion
- [x] 18.4 Vista de lista de almacen (`/almacen`) con columna "Dias en almacen"
       - Badge rojo si supera `diasMaximoAlmacenamiento`
       - Ordenar por `fechaEntradaAlmacen` ASC (FIFO visual)
- [x] 18.5 Timeline en detalle de residuo: entrada → dias en almacen → salida (o alerta)
- [x] 18.6 Alertas en Dashboard: "X residuos superan el tiempo maximo de almacenamiento"
- [x] 18.7 API endpoints:
       - `GET /api/recogidas` (lista con filtros: estado, fecha, centro)
       - `POST /api/recogidas`
       - `PUT /api/recogidas/{id}`
       - `DELETE /api/recogidas/{id}`
       - `GET /api/almacen/alertas-fifo` (residuos fuera de plazo)
- [ ] 18.8 Tests: RecogidaService, alertas FIFO

**Libreria frontend:** FullCalendar.js v6 (CDN, gratis para uso basico)
```html
<script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.15/index.global.min.js"></script>
```

---

### FASE 19 — Modulo Documentos (cumplimiento legal RD 553/2020)

**Objetivo:** Gestionar todos los documentos legales obligatorios en la gestion de residuos.
Asegurar que cada traslado/recogida lleva su documentacion asociada y que el archivo
cronologico se mantiene automaticamente.

**Marco legal:**
Segun RD 553/2020, Ley 7/2022 y fuentes sectoriales (DCD, AEDED, ATREVA):

| Documento | Cuando es obligatorio | Plazo |
|-----------|----------------------|-------|
| Contrato de tratamiento | Siempre; entre productor y gestor autorizado | Previo al primer traslado |
| Notificacion Previa (NP) | Residuos peligrosos o domesticos mezclados | Min. 10 dias antes del traslado |
| Documento de Identificacion (DI) | Todos los traslados; uno por tramo logistico | Emitir al envio, cerrar en recepcion |
| Archivo Cronologico | Obligatorio para todos los productores | Conservar minimo 3 anos |
| Ficha de Aceptacion (FA) | Emitida por la planta de tratamiento al recibir | Al llegar al destino |
| Hoja de Seguimiento (HS/HSI) | Segun comunidad autonoma | Al envio |
| Informe Final de Gestion | Al cierre de contrato o periodo | Anual o por proyecto |

**Modelo de datos nuevo:**

```java
Documento {
  Long id;
  TipoDocumento tipo;           // enum: CONTRATO, NP, DI, ARCHIVO_CRONOLOGICO, FA, HS, INFORME
  String numeroReferencia;      // ej: "DI-2024-001"
  Traslado traslado;            // FK nullable (DI, NP, FA van asociados a traslado)
  Recogida recogida;            // FK nullable
  Centro centro;                // FK nullable (contratos van por centro)
  LocalDate fechaEmision;
  LocalDate fechaVencimiento;   // para NP: fecha del traslado - 10 dias
  LocalDate fechaCierre;        // para DI: fecha en que el gestor confirma
  EstadoDocumento estado;       // BORRADOR, EMITIDO, CERRADO, VENCIDO
  String rutaArchivo;           // path del PDF subido o generado
  String observaciones;
  LocalDateTime creadoEn;
}
```

**Tareas:**

- [x] 19.1 Crear entidades `Documento`, `TipoDocumento` (enum), `EstadoDocumento` (enum)
- [x] 19.2 DocumentoRepository + DocumentoService
- [x] 19.3 Vista `/documentos` con tabla: tipo, referencia, centro/traslado, fechas, estado, acciones
       - Filtros: por tipo, por estado, por fecha, por centro
       - Badge de alerta si `fechaVencimiento` < hoy + 10 dias
- [x] 19.4 CRUD de documentos (crear/editar/eliminar)
- [x] 19.5 Upload de archivos PDF (guardar en `src/main/resources/static/documentos/` o ruta config)
       - Validar que solo se suben PDFs, max 10MB
       - Endpoint: `POST /api/documentos/{id}/archivo`
- [x] 19.6 Generacion automatica de DI al crear/completar una Recogida
       - El DI se genera como PDF con: LER, cantidad, origen, destino, transportista, fechas
       - Usar la `PdfService` existente (Fase 15) o crearla aqui
- [x] 19.7 Archivo Cronologico automatico: cada traslado/recogida completada genera entrada
       - Vista especial `/documentos/archivo-cronologico`: tabla cronologica
       - Exportable a PDF/CSV para auditorias
- [x] 19.8 Alerta NP en Dashboard: "X recogidas programadas sin NP emitida con menos de 10 dias"
- [x] 19.9 API endpoints:
       - `GET /api/documentos` (con filtros)
       - `POST /api/documentos`
       - `PUT /api/documentos/{id}`
       - `DELETE /api/documentos/{id}`
       - `GET /api/documentos/{id}/archivo` (descargar PDF)
       - `POST /api/documentos/{id}/archivo` (subir PDF)
       - `GET /api/documentos/archivo-cronologico` (exportar)
- [x] 19.10 Tests: DocumentoService, generacion DI, alertas NP

**Nombre normalizado de archivos (buena practica legal):**
`AAAA-MM-DD_TIPO_Centro-CodigoLER_Destino.pdf`
Ejemplo: `2024-03-15_DI_CentroNorte-160107_GestorAutorizado.pdf`

---

### FASE 20 — Modulo Informes

**Objetivo:** Generar informes de gestion para auditorias internas, inspecciones y
cumplimiento anual. Cubrir el Informe Final de Gestion de Residuos (obligatorio legal).

**Tipos de informe:**

| Informe | Contenido | Formato |
|---------|-----------|---------|
| Estadisticas por periodo | Recogidas, cantidades, LER, centros | PDF/CSV |
| Inventario de almacen | Residuos activos con dias en almacen | PDF |
| Trazabilidad de residuo | Historia completa de un residuo (LER > almacen > recogida > destino) | PDF |
| Archivo cronologico | Todas las operaciones en periodo | CSV/PDF |
| Informe Final de Gestion | Formato legal: LER, cantidades, porcentaje reciclado, agentes | PDF |
| Checklist de auditoria | Estado de documentacion: DIs cerrados, NPs vigentes, contratos activos | HTML/PDF |

**Tareas:**

- [ ] 20.1 Vista `/informes` con lista de tipos de informe disponibles
- [ ] 20.2 Formulario de parametros por informe (rango de fechas, centro, LER)
- [ ] 20.3 Informe de estadisticas por periodo (extiende el dashboard actual)
       - Residuos gestionados por LER
       - Cantidad total por unidad
       - Recogidas completadas vs pendientes
       - Exportar a PDF y CSV
- [ ] 20.4 Informe de inventario de almacen (estado actual del almacen)
       - Tabla: LER, descripcion, cantidad, dias en almacen, alerta FIFO
- [ ] 20.5 Informe de trazabilidad por residuo
       - Timeline: entrada almacen → recogidas → destino final
       - Documentos asociados (DI, NP, FA)
- [ ] 20.6 Informe Final de Gestion (formato legal segun AEDED)
       - Tabla resumen cuantitativa: LER, prevision EGR, real, variacion
       - Porcentaje de valoracion/reciclaje/eliminacion
       - Agentes participantes (centros, transportistas, gestores)
       - Reflexion sobre impacto ambiental (campo de texto libre)
- [ ] 20.7 Checklist de auditoria pre-inspeccion
       - Para cada traslado/recogida del periodo: tiene DI cerrado?, tiene NP vigente?, contrato activo?
       - Semaforo verde/amarillo/rojo por item
- [ ] 20.8 PdfService: plantillas para cada tipo de informe (usar OpenPDF)
- [ ] 20.9 API:
       - `GET /api/informes/estadisticas?desde=&hasta=&centroId=` (JSON para frontend)
       - `GET /api/informes/inventario-almacen` (JSON)
       - `GET /api/informes/trazabilidad/{residuoId}` (JSON)
       - `GET /api/informes/final-gestion?periodo=` (genera y devuelve PDF)
       - `GET /api/informes/checklist-auditoria?periodo=` (JSON/PDF)
- [ ] 20.10 Tests: generacion de informes, calculos de estadisticas

---

### FASE 21 — Modulo Negocio y perfil de empresa

**Objetivo:** Registrar los datos legales de la empresa: NIMA, autorizaciones, seguros,
datos de contacto. Necesario para pre-rellenar documentos (DI, NP, contratos).

**Tareas:**

- [x] 21.1 Entidad `Empresa` (singleton: solo un registro por instancia)
       - nombre, CIF, NIMA (Numero de Identificacion Medioambiental), direccion, telefono, email
       - autorizacionGestor (texto/referencia), autorizacionTransportista
       - logoUrl (para PDF)
- [x] 21.2 Vista `/negocio` con formulario de edicion (solo ADMIN)
- [ ] 21.3 Integrar datos de empresa en la generacion de PDFs
- [x] 21.4 Vista `/mis-datos` para que cada usuario edite su perfil
       - Nombre, email, password (con confirmacion)
       - No puede cambiar su propio rol

---

### Orden de ejecucion (Bloque II)

| Prioridad | Fase | Justificacion |
|-----------|------|---------------|
| 1 | 17 (navegacion) | Base visual para todo lo demas. Baja complejidad, alto impacto. |
| 2 | 18 (calendario + FIFO) | Feature diferencial. Requiere nuevas entidades (Recogida). |
| 3 | 19 (documentos) | Nucleo legal del producto. Bloquea los informes. |
| 4 | 20 (informes) | Depende de documentos y FIFO. Cierre del producto. |
| 5 | 21 (negocio) | Datos de empresa necesarios para PDFs correctos. |

### Dependencias Bloque II

```
17 (nav) → independiente, puede ir primero
18 (FIFO + recogidas) → requiere 17 terminado en UI
19 (documentos) → requiere 18 (Recogida existe antes que Documento)
20 (informes) → requiere 18 + 19 (datos completos)
21 (negocio) → requiere 17, paralelo con 18
```

---

### Resumen de nuevas entidades a crear (Bloque II)

| Entidad | Fase | Descripcion |
|---------|------|-------------|
| `Recogida` | 18 | Recogida programada con fechas, estado y transportista |
| `Documento` | 19 | Documento legal (DI, NP, Contrato, etc.) vinculado a traslado/recogida |
| `Empresa` | 21 | Datos legales de la empresa (singleton) |
| Campos en `Residuo` | 18 | `fechaEntradaAlmacen`, `fechaSalidaAlmacen`, `diasMaximoAlmacenamiento` |

---

## BLOQUE III — App movil Android (Kotlin)

### Analisis de base de datos compartida

**Requisito:** web y movil acceden a los mismos datos en tiempo real, multiples usuarios concurrentes.

#### Por que SQLite NO sirve para esto

SQLite es un motor embebido que escribe en un archivo local. Solo permite un escritor a la vez
y no admite conexiones remotas. Es adecuado para desarrollo local de un solo proceso, pero no
puede ser compartido entre la app web en el servidor y la app movil en un dispositivo Android.

**Conclusion: SQLite queda como base de datos de desarrollo local unico. Nunca en produccion compartida.**

#### Por que PostgreSQL ES la solucion (ya planificada)

PostgreSQL es un servidor de base de datos real con las caracteristicas necesarias:

| Caracteristica | Valor |
|---------------|-------|
| Conexiones simultaneas | Hasta miles (configurable con pgBouncer) |
| Aislamiento de transacciones | ACID completo |
| Acceso remoto | Si, via TCP/IP |
| Modelo de usuarios y permisos | Si, nativo |
| Escalado | Vertical y horizontal (replicas de lectura) |

**Ya esta planificado en este proyecto:** `application-prod.properties` apunta a PostgreSQL.
No hay que cambiar nada del modelo de datos ni de los repositorios JPA.

#### Arquitectura de acceso compartido

```
┌──────────────────────────────────────────────────┐
│                  CLIENTES                         │
│                                                   │
│  [Navegador Web]      [App Android (Kotlin)]      │
│       │                      │                   │
│       └──────────┬───────────┘                   │
└──────────────────│───────────────────────────────┘
                   │  HTTPS + JSON
                   ▼
┌──────────────────────────────────────────────────┐
│          Spring Boot API (unico punto de entrada) │
│   - Autenticacion (sesion para web, JWT para movil)│
│   - Control de acceso por rol                     │
│   - Logica de negocio                             │
│   - JPA / Hibernate                               │
└──────────────────────────────────────────────────┘
                   │  JDBC / JPA
                   ▼
┌──────────────────────────────────────────────────┐
│              PostgreSQL (produccion)              │
│   - Una sola instancia                            │
│   - Todas las entidades existentes sin cambios    │
└──────────────────────────────────────────────────┘
```

**Regla clave:** ningun cliente (web ni movil) accede directamente a la BD.
Siempre a traves de la API. La BD no sabe si el cliente es un navegador o una app Android.

---

### FASE M0 — Preparacion del backend para clientes moviles

**Objetivo:** Anadir autenticacion JWT al backend sin romper la autenticacion de sesion que
usa la web. Los endpoints `/api/**` deben aceptar tanto sesion como token JWT.

**Por que hace falta JWT para movil:**
La app Android no puede gestionar cookies de sesion (JSESSIONID) de forma fiable.
El estandar para APIs consumidas por apps moviles es JWT: el cliente envia el token
en la cabecera `Authorization: Bearer <token>` en cada peticion. Es stateless: el servidor
no guarda estado de sesion.

**Tareas:**

- [ ] M0.1 Anadir dependencia `spring-boot-starter-oauth2-resource-server` o `jjwt` al pom.xml
- [ ] M0.2 Crear `JwtService`: genera y valida tokens JWT firmados con clave secreta configurable
       - Payload: `sub` (email), `rol`, `exp` (expiracion, default 24h)
       - Clave configurable via variable de entorno `JWT_SECRET`
- [ ] M0.3 Crear `JwtAuthFilter`: intercepta peticiones `/api/**` y valida el token si presente
       - Si hay token valido: autentica en SecurityContext (sin sesion)
       - Si no hay token: sigue la cadena (la sesion puede autenticar)
       - Si el token es invalido: 401
- [ ] M0.4 Crear endpoint `POST /api/auth/login`
       - Body: `{ "email": "...", "password": "..." }`
       - Response: `{ "token": "...", "rol": "...", "nombre": "...", "expira": "..." }`
       - Misma logica de autenticacion que el formulario de login actual
- [ ] M0.5 Actualizar `SeguridadConfig` para registrar `JwtAuthFilter` antes de `UsernamePasswordAuthenticationFilter`
- [ ] M0.6 Ampliar CORS: permitir cualquier origen en `/api/**` (la app Android no tiene origen HTTP)
       - O configurar dominio de produccion especifico si se despliega
- [ ] M0.7 Tests: login JWT, peticion autenticada con token, token expirado, token invalido
- [ ] M0.8 Documentar en Swagger: `@SecurityScheme` de tipo `http bearer`

**Nota sobre seguridad:**
- El JWT_SECRET debe tener al menos 256 bits de entropia (32 caracteres aleatorios)
- No hardcodear el secreto: solo via variable de entorno
- Tokens de refresco (refresh tokens) son opcionales para esta fase; se puede implementar

---

## Handoff — 2026-04-22

**Estado actual:** Fases 1–19 completas + Fase 21 (Empresa). Fase 20 (Informes) y Fase 22+ pendientes.

### Ultimas tareas cerradas en esta sesion
- 17.8 Breadcrumb en header (mapping pageId -> grupo en `layouts/main.html`)
- 18.5 Timeline residuo (boton "ver" + modal con timeline entrada/almacen/salida en `residuos.html`)
- 21.* Modulo Empresa verificado (entidad + `/negocio` + `/mis-datos` ya estaban)
- 19.5 Upload de PDFs para documentos (campo `archivoUrl`, `POST /api/documentos/{id}/upload`, `GET /api/documentos/{id}/archivo`, `UploadsWebConfig` sirve `/uploads/documentos/**`, UI en `documentos.html`)
- 19.7 Archivo Cronologico automatico (hook en `TrasladoServiceDB.cambiarEstado` cuando pasa a `COMPLETADO`, mismo lock que el DI, referencia `AC-{anio}-{NNN}`)
- 19.8 Alerta NP en dashboard (`GET /api/documentos/alertas/notificacion-previa` + tarjeta `panelNp` en `index.html`)
- 19.10 Tests `DocumentoServiceTest` (7 tests, todos OK)

### Como arrancar el entorno en casa
1. **JDK:** `JAVA_HOME=C:\Users\afp5\.jdk\jdk-25` (Java 25, requerido por Spring Boot 4.0.5).
2. **Compilar:** desde `ServidorApiRest/`, `.\mvnw.cmd -q -DskipTests compile`.
3. **Arrancar:** `.\mvnw.cmd spring-boot:run`. Servidor en `http://localhost:8080`.
4. **Tests:** `.\mvnw.cmd test` o filtrar con `"-Dtest=NombreTest"`.
5. **Login default:** ver `data.sql` / seeder (admin@balmis.com).

### Convenciones del proyecto a tener en cuenta
- **NO crear branches** en este repo (memoria `/memories/repo/no-branches-servidorApiRest.md`). Trabajar siempre sobre `main`.
- **Thymeleaf:** usar `th:onclick="|funcion(${id})|"` (sintaxis literal) para evitar errores de parseo (`/memories/repo/thymeleaf-onclick-fix.md`).
- **CSS:** los layouts cargan `app.css`, NO `styles.css`. Cualquier override (SweetAlert, etc.) va en `app.css`.
- **Spring Security:** estaticos publicos van en el array `permitAll` de `SeguridadConfig` (`/logo.png`, `/favicon.ico`, `/images/**`, `/js/**`, `/uploads/documentos/**` lo gestiona la sesion).
- **Auto-generacion de documentos:** al pasar un Traslado a COMPLETADO se crean DI (`DI-YYYY-NNN`) y Archivo Cronologico (`AC-YYYY-NNN`) idempotentes y serializados con `DI_REFERENCIA_LOCK`.
- **Uploads:** PDF max 10 MB, validados por mimetype, guardados en `${ecoadmin.uploads.documentos:uploads/documentos}` con nombre `{yyyyMMdd}_{id}_{uuid8}.pdf`.

### Siguiente bloque sugerido — Fase 20 Informes (orden recomendado)
1. **20.1** Vista `/informes` con lista de tipos disponibles (cards con descripcion + boton "Generar").
2. **20.2** Formulario de parametros por informe (rango fechas, centro, LER) — modal o pagina dedicada.
3. **20.8** `PdfService` con plantillas OpenPDF (ya hay `pom.xml` con OpenPDF segun el codigo de `DocumentoController`).
4. **20.3** Informe estadisticas por periodo (reusar logica de `/api/estadisticas`).
5. **20.4** Inventario almacen actual (reusar `/api/almacen/...`).
6. **20.5** Trazabilidad por residuo (timeline por LER).
7. **20.6** Informe Final de Gestion (formato AEDED — ver `RD 553/2020` art. 8).
8. **20.7** Checklist auditoria pre-inspeccion.
9. **20.9** API: `GET /api/informes/{tipo}?desde&hasta&centroId&ler` -> PDF.
10. **20.10** Tests `InformeServiceTest`.

Otras tareas pendientes notables:
- **18.8** Tests `RecogidaService` + alertas FIFO (rapido — copiar patron de `DocumentoServiceTest`).
- **16.5.\*** Consolidacion Tailwind/CSS (10 subtareas — bloque grande de refactor frontend).
- **M0** Auth JWT para futura app Android.

### Como retomar
> "Continua con la Fase 20 Informes segun el orden del Handoff en PLAN_PROYECTO.md. Empieza por 20.1 + 20.8 (vista + PdfService base)."

  con un endpoint `POST /api/auth/refresh` en el futuro

---

### FASE M1 — App Android (Kotlin + Jetpack Compose)

**Stack tecnico:**

| Capa | Libreria | Version recomendada |
|------|----------|---------------------|
| UI | Jetpack Compose | BOM 2024.x |
| Navegacion | Navigation Compose | 2.8.x |
| HTTP | Retrofit2 + OkHttp | 2.11 / 4.12 |
| JSON | Gson / Moshi | Gson 2.10 |
| Estado | ViewModel + StateFlow | Lifecycle 2.8.x |
| Auth storage | EncryptedSharedPreferences | Security Crypto 1.1 |
| Inyeccion | Hilt | 2.51 |
| Imagenes | Coil | 2.6 |

**Prerequisito:** FASE M0 completada (endpoint JWT disponible).

**Pantallas planeadas (mapeadas a endpoints existentes):**

| Pantalla | Endpoint API | Roles con acceso |
|----------|--------------|-----------------|
| Login | `POST /api/auth/login` | Todos |
| Dashboard | `GET /api/estadisticas/**` | Todos |
| Traslados / Recogidas | `GET/POST/PATCH /api/traslados` | PRODUCTOR, GESTOR, TRANSPORTISTA, ADMIN |
| Detalle traslado + historial | `GET /api/traslados/{id}` | Segun ownership |
| Cambio de estado | `PATCH /api/traslados/{id}/estado` | TRANSPORTISTA, GESTOR |
| Centros | `GET/POST/PUT /api/centros` | PRODUCTOR, GESTOR, ADMIN |
| Residuos | `GET/POST/PUT /api/residuos` | PRODUCTOR, GESTOR, ADMIN |
| Usuarios (admin) | `GET /api/usuarios` | ADMIN |
| Mi perfil | `GET /api/usuarios/me` (pendiente) | Todos |
| Escaner QR | `GET /api/qr/{id}` | Todos |

**Estructura de paquetes Android:**

```
com.iesdoctorbalmis.ecoadmin
+-- data/
|   +-- api/
|   |   +-- ApiClient.kt          Retrofit singleton + interceptor JWT
|   |   +-- TrasladoApi.kt        Interface Retrofit
|   |   +-- CentroApi.kt
|   |   +-- ResiduoApi.kt
|   |   +-- AuthApi.kt
|   +-- model/                    Data classes espejo de los DTOs del backend
|   +-- repository/               Repositorios que llaman a la API
+-- ui/
|   +-- login/                    LoginScreen + LoginViewModel
|   +-- dashboard/                DashboardScreen + DashboardViewModel
|   +-- traslados/                TrasladosScreen, DetalleScreen, ViewModels
|   +-- centros/
|   +-- residuos/
|   +-- theme/                    MaterialTheme, colores, tipografia
+-- di/
|   +-- NetworkModule.kt          Hilt: provee Retrofit, ApiClients
|   +-- RepositoryModule.kt
+-- MainActivity.kt
+-- NavGraph.kt                   Definicion de rutas de navegacion
```

**Tareas:**

- [ ] M1.1 Crear proyecto Android en Android Studio (minSdk 26, targetSdk 35)
       - Activar Jetpack Compose, Hilt en el wizard
- [ ] M1.2 Configurar Retrofit con interceptor que inyecta `Authorization: Bearer <token>`
       - Token almacenado en `EncryptedSharedPreferences` (nunca en texto plano)
- [ ] M1.3 Pantalla de login con validacion de campos
       - Llamar a `POST /api/auth/login`, guardar token y rol
       - Redirigir a Dashboard si ya hay token valido en storage
- [ ] M1.4 Dashboard: cards con totales de traslados por estado (llamar a `/api/estadisticas`)
- [ ] M1.5 Lista de traslados con pull-to-refresh y filtro por estado
- [ ] M1.6 Detalle de traslado: datos completos + timeline de historial + boton cambio de estado
- [ ] M1.7 Lista y creacion de centros
- [ ] M1.8 Lista y creacion de residuos
- [ ] M1.9 Escaner QR integrado (CameraX + MLKit para leer QR de un traslado)
- [ ] M1.10 Manejo de errores: token expirado → redirigir a login, 403 → mensaje de acceso denegado
- [ ] M1.11 Soporte offline basico: cachear ultima respuesta de listas con Room (opcional, fase posterior)

**Directorio del proyecto Android:**
`C:/Users/afp5/Git/servidor_api/ecoadmin-android/` (worktree independiente o repo separado)

---

### Orden de ejecucion Bloque III

| Prioridad | Fase | Prerequisito | Duracion estimada |
|-----------|------|--------------|-------------------|
| 1 | M0 (JWT backend) | Ninguno | 1-2 dias |
| 2 | M1.1-M1.3 (proyecto + login) | M0 | 1 dia |
| 3 | M1.4-M1.6 (traslados) | M1.3 | 2-3 dias |
| 4 | M1.7-M1.9 (centros, residuos, QR) | M1.3 | 2 dias |
| 5 | M1.10-M1.11 (pulido + offline) | M1.4-M1.9 | 1-2 dias |

---

### Mapa curricular — Kotlin Android en el IES

El proyecto EcoAdmin Android esta disenado para crecer en paralelo al temario del curso.
Cada bloque del aula tiene ejercicios directamente aplicables en este proyecto real.

**Nota sobre Room vs Retrofit vs Firestore:**
El temario cubre tres formas distintas de acceder a datos, con roles muy distintos en este proyecto:
- **Room** (Tema 5.1): base de datos SQLite LOCAL en el dispositivo. Se usa para cache offline.
  No es la BD compartida — es solo un buffer del dispositivo.
- **Retrofit** (Tema 5.2): libreria HTTP para llamar a nuestra API Spring Boot. ESTA es la conexion
  real a los datos compartidos (PostgreSQL via API).
- **Firestore** (Tema 5.3): BD en la nube de Google (Firebase). NO se usa en este proyecto
  porque ya tenemos Spring Boot + PostgreSQL. Se estudia como alternativa al stack propio,
  util para proyectos sin backend propio. Puedes hacer los ejercicios del tema con un proyecto
  de prueba separado.

| Semana | Fechas | Bloque / Tema | Aplicacion en EcoAdmin Android |
|--------|--------|---------------|-------------------------------|
| 1 | 8-21 sep | B1: Kotlin basico, entorno, sintaxis | Crear data classes del proyecto: `Traslado`, `Centro`, `Residuo`, `Usuario`. Practicar null safety, when, extension functions. |
| 2 | 22 sep - 5 oct | Colecciones y Lambdas. B2: arquitectura, proyecto plantilla | Crear el proyecto Android con la estructura de paquetes de EcoAdmin. Escribir funciones de transformacion de listas (filtrar traslados por estado, agrupar por centro). |
| 3 | 6-19 oct | B3: Jetpack Compose, maquetacion, Material Design | Implementar M1.1 y M1.3: pantalla de login con campos validados y pantalla de dashboard con cards de estado. Sin ViewModel todavia (estado local con `remember`). |
| 4 | 20 oct - 2 nov | ViewModel, Hilt | Refactorizar login y dashboard para usar ViewModel + StateFlow. Configurar Hilt (M1.2 parcial: NetworkModule con Retrofit apuntando a localhost). |
| 5 | 3-16 nov | Lazy lists y cuadriculas | Implementar M1.5: lista de traslados con `LazyColumn`, pull-to-refresh. Implementar lista de centros y residuos. Datos mockeados localmente (sin API real todavia). |
| 6 | 1-14 dic | B4: Corrutinas, Intents y contracts | Conectar los repositorios a coroutines (`viewModelScope.launch`, `suspend`). Usar Intent para compartir QR o abrir mapa con coordenadas de una direccion. |
| 7 | 15 dic - 11 ene | Scaffolds y menus, Navegacion | Implementar M1.1 completo: Scaffold con BottomNavigationBar o NavigationDrawer. NavGraph con rutas login → dashboard → detalle traslado. |
| 8 | 12-25 ene | Room (acceso a datos local) | Implementar M1.11: cache offline con Room. Guardar en BD local la ultima respuesta de `/api/traslados`. Mostrar datos cacheados cuando no hay red. |
| 9 | 26 ene - 8 feb | Retrofit | Implementar M0 (JWT en backend) y conectar la app a la API real. M1.2 completo: interceptor JWT. Sustituir datos mockeados por llamadas Retrofit reales a Spring Boot. |
| 10 | 23 feb - 8 mar | Firestore | Ejercicios del tema con proyecto separado de prueba. EcoAdmin ya tiene su backend; Firestore no aplica. Aprovechar para hacer los ejercicios del aula y comparar los dos modelos (BaaS vs API propia). |

**Hitos del proyecto alineados con evaluaciones:**

| Evaluacion | Fecha | Estado esperado del proyecto |
|------------|-------|------------------------------|
| Primera (nov) | 17-30 nov | Pantallas de login, dashboard y lista de traslados con datos mockeados. Compose + ViewModel + Hilt funcionando. |
| Segunda (feb) | 9-22 feb | App conectada a la API real via Retrofit + JWT. Navegacion completa. Cache Room opcional. |
| Tercera / FCT | mar-jun | App pulida: manejo de errores, escaner QR, offline cache, CRUD completo de traslados. |

**Consejo:** usa el emulador de Android Studio apuntando a `http://10.0.2.2:8080` para
conectar con el servidor Spring Boot corriendo en tu maquina local (10.0.2.2 es el alias
del host en el emulador Android).

