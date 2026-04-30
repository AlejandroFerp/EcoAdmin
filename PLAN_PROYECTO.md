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
- [x] Rutas legacy y navegación pública consolidadas en ZonaPublicaController
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

### FASE 16.5 — Deuda tecnica frontend (consolidacion CSS/Tailwind) [COMPLETADA]

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
- [x] 16.5.1 Instalar Tailwind como dependencia npm + build a un unico `app.css` minificado con purge/content scan (objetivo: pasar de ~3 MB a ~10 KB).
- [x] 16.5.2 Configurar `tailwind.config.js` con los tokens `--eco-*` integrados como utilidades (`bg-eco-primary`, `text-eco-danger`, etc.).
- [x] 16.5.3 Elegir UNA libreria de componentes (DaisyUI O Flowbite) y eliminar la otra.
- [x] 16.5.4 Mover el `<style>` inline de `main.html` a `app.css` (separar en bloques: tipografia, scrollbars, form-control-eco, kanban, nav).
- [x] 16.5.5 Unificar `styles.css` dentro de `app.css` y reemplazar colores hardcodeados por `var(--eco-*)` para que un cambio de token propague a todas las clases `.ea-*`.
- [x] 16.5.6 Cargar `app.css` desde `layouts/main.html` UNICAMENTE (eliminar carga duplicada en `header.html`).
- [x] 16.5.7 Eliminar reglas sobre tags globales (`table`, `td`, `.navbar`, `.jumbotron`, `footer`) o convertirlas en clases (`.ea-table`, `.ea-table-cell`).
- [x] 16.5.8 Decidir politica para `login.html` y `preview.html` (paginas standalone): o usan el layout o documentar por que no.
- [x] 16.5.9 Anadir lint de CSS (Stylelint con `stylelint-config-standard`) y un script `npm run css:build` integrado en el ciclo de Maven (frontend-maven-plugin) para que `mvn package` produzca el CSS final.
- [x] 16.5.10 Eliminar comentarios "Phase 11.3" y similares del codigo de produccion.

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
|   +-- IndexController           Redirect / -> /dashboard
|   +-- ZonaPublicaController     Vistas Thymeleaf
|   +-- UsuarioController         API CRUD (solo ADMIN)
|   +-- CentroController          API CRUD + ownership
|   +-- ResiduoController         API CRUD + ownership
|   +-- TrasladoController        API CRUD + estado + historial
|   +-- DireccionController       API CRUD
|   +-- EstadisticasController    API agregados
|   +-- ListaLerController        API catalogo LER (busqueda)
|   +-- QrController              API generacion QR + entrada via escaneo
|   +-- RutaController            API CRUD rutas + activas filtradas
|   +-- PerfilTransportistaController  API perfil transportista + calculo tarifa
+-- dto/
|   +-- UsuarioDTO                Record sin password
|   +-- PerfilEdicionDTO          Record edicion perfil
|   +-- CambioPasswordDTO         Record cambio password
+-- modelo/
|   +-- Usuario                   id, codigo, nombre, email, password, rol, fechaAlta
|   +-- Centro                    id, codigo, usuario, nombre, tipo, direccion(FK), nima, telefono, email, nombreContacto
|   +-- Residuo                   id, codigo, cantidad, unidad, estado, codigoLER, descripcion, centro(FK), fechas almacen
|   +-- Direccion                 id, codigo, nombre, descripcion, calle, calle2, ciudad, codigoPostal, provincia, pais, lat, lon
|   +-- Traslado                  id, codigo, centroProductor, centroGestor, residuo, transportista, ruta(FK), estado, fechas, observaciones
|   +-- EventoTraslado            id, traslado, estadoAnterior, estadoNuevo, fecha, comentario, usuario, origen(QR_SCAN|MANUAL)
|   +-- Recogida                  id, codigo, residuo, centroOrigen, centroDestino, transportista, fechas, estado
|   +-- Documento                 id, codigo, tipo, traslado(FK), recogida(FK), centro(FK), fechas, estado, rutaArchivo
|   +-- Empresa                   id, codigo, nombre, CIF, NIMA, direccion, telefono, email, autorizaciones, logoUrl
|   +-- Adjunto                   id, nombre, rutaArchivo, traslado(FK nullable), recogida(FK nullable), usuario(FK nullable)
|   +-- PerfilTransportista       id, codigo, usuario(OneToOne), matricula, formulaTarifa, unidadTarifa
|   +-- Ruta                      id, codigo, nombre, transportista(FK), origen(FK Direccion), destino(FK Direccion), distanciaKm, formulaTarifa
|   +-- SecuenciaCodigo           prefijo(PK), ultimo (para generacion atomica de codigos)
|   +-- ListaLer                  id, codigo, descripcion (953 codigos europeos)
|   +-- enums/Rol                 PRODUCTOR, GESTOR, TRANSPORTISTA, ADMIN
|   +-- enums/EstadoTraslado      Libertad total entre estados (rectificable, con historial)
|   +-- enums/EstadoRecogida      PROGRAMADA, EN_CURSO, COMPLETADA, CANCELADA
|   +-- enums/TipoDocumento       CONTRATO, NP, DI, ARCHIVO_CRONOLOGICO, FA, HS, INFORME
|   +-- enums/EstadoDocumento     BORRADOR, EMITIDO, CERRADO, VENCIDO
+-- repository/                   JpaRepository para cada entidad
+-- servicios/
|   +-- CodigoService             Generacion atomica de codigos legibles por entidad
|   +-- TarifaValidator           Validacion y evaluacion de formulas con exp4j
|   +-- [servicio por entidad]    Interface + DB impl
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
- **exp4j** (`net.objecthunter:exp4j:0.4.8`): evaluador de expresiones matematicas para formulas de tarifa; sin reflection ni eval, seguro contra inyeccion
- **Leaflet Routing Machine** (CDN, MIT): plugin de Leaflet para dibujar rutas reales siguiendo carreteras
- **OSRM** (`https://router.project-osrm.org`): servidor de routing gratuito (OpenStreetMap), sin API key; para produccion usar OpenRouteService (2.000 req/dia gratis con API key) o self-hosted OSRM
- **html5-qrcode** (CDN): libreria JS para escaner QR desde camara del navegador (WebRTC); sin instalacion nativa
- **Google Maps**: NO usar — requiere tarjeta de credito y cobra por request; OSRM+Leaflet es el equivalente libre

---

## Registro vivo de bugs y soluciones

Esta seccion queda inaugurada como memoria operativa del proyecto para incidencias que cuesten muchas iteraciones diagnosticar o cerrar.

**Regla de uso:** cuando un problema nos haga perder varias iteraciones, aqui se documenta en formato fijo: **Bug**, **Causa raiz**, **Solucion confirmada** y **Precaucion**. La idea es no volver a tropezar con la misma piedra y reducir debugging especulativo.

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

### 7. Runtime local desalineado con el stack del proyecto
**Bug:** `application.properties` habia quedado apuntando por defecto a PostgreSQL en `localhost:5432`, asi que el proyecto no arrancaba en este equipo si no habia un servidor PostgreSQL local levantado.
**Causa raiz:** La configuracion base dejo de reflejar el stack declarado del proyecto (`SQLite dev / H2 test / PostgreSQL prod`) y el perfil por defecto dejo de ser usable para desarrollo local.
**Solucion confirmada:** Restaurar `application.properties` para que use `jdbc:sqlite:ecoadmin.db` por defecto y mantener PostgreSQL en `application-prod.properties`.
**Precaucion:** Si se activa `SPRING_PROFILES_ACTIVE=prod`, el arranque vuelve a requerir PostgreSQL disponible. Ademas, `mvn spring-boot:run` sigue pasando por compilacion de tests y hoy puede fallar por `DocumentoE2ETest`; para trabajar localmente aqui, usar la ejecucion directa de la aplicacion o lanzar Maven con `-Dmaven.test.skip=true` mientras ese test se adapta.

## Plan operativo de entornos

### Entorno actual (este equipo) — seguir con SQLite
- Objetivo: poder continuar desarrollo sin depender de PostgreSQL local.
- Configuracion: `application.properties` usa SQLite por defecto en `ServidorApiRest/ecoadmin.db`.
- Requisito: JDK 25 en `C:\Users\afp5\.jdk\jdk-25`.
- Ejecucion recomendada: arrancar la aplicacion sin perfil `prod`, desde el modulo `ServidorApiRest`.
- Resultado esperado: la aplicacion crea o reutiliza `ecoadmin.db` y no intenta conectar a `localhost:5432`.

### Entorno de casa — PostgreSQL local
- Objetivo: probar el mismo stack de produccion, pero en local.
- Activar perfil: `SPRING_PROFILES_ACTIVE=prod`.
- Variables minimas: `DATABASE_URL=jdbc:postgresql://localhost:5432/ecoadmin`, `DATABASE_USER`, `DATABASE_PASSWORD`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`.
- Requisito previo: tener PostgreSQL escuchando realmente en `localhost:5432` y con la base `ecoadmin` creada.
- Efecto: `application-prod.properties` sobreescribe el datasource y deja de usarse el archivo SQLite.

### Checklist de migracion a PostgreSQL local
1. Instalar o arrancar PostgreSQL local.
2. Crear base de datos `ecoadmin` y un usuario con permisos sobre ella.
3. Exportar `SPRING_PROFILES_ACTIVE=prod`.
4. Exportar `DATABASE_URL`, `DATABASE_USER` y `DATABASE_PASSWORD`.
5. Mantener `ADMIN_EMAIL` y `ADMIN_PASSWORD` definidos.
6. Arrancar la aplicacion y verificar que Hibernate conecta a PostgreSQL en lugar de `jdbc:sqlite:ecoadmin.db`.
7. Si el arranque se hace con Maven y falla en `DocumentoE2ETest`, migrar ese test fuera de `AutoConfigureMockMvc` o usar temporalmente `-Dmaven.test.skip=true` solo para el run local.
**Precaucion:** Verificar que el target/classes tiene la version actualizada del template.

### 7. Archivo login.html duplicado
**Bug:** Al recrear login.html, el contenido viejo se pego al nuevo resultando en 2 documentos HTML consecutivos en el mismo archivo. El navegador renderizo ambos forms.
**Solucion:** Verificar longitud del archivo despues de crear/editar. Truncar si tiene contenido sobrante.
**Precaucion:** Al reescribir un archivo completo, verificar que el resultado tiene exactamente 1 estructura HTML.

### 8. Preview PDF de traslados en modal
**Bug:** El detalle de traslados fallo repetidamente al intentar mostrar el PDF real en un `iframe`/`object`/`embed`: a veces forzaba descarga, a veces mostraba blanco, y con la primera version de `pdf.js` el zoom rompia el visor con `ArrayBuffer at index 0 is already detached`.
**Causa raiz:** El endpoint backend estaba sano; el problema real era el visor PDF embebido del navegador/contexto usado durante la depuracion, que abortaba la carga inline. Al pasar a `pdf.js`, el zoom seguia fallando porque se reutilizaba el mismo `ArrayBuffer` entre renders y el worker lo dejaba detached tras el primer parseo.
**Solucion confirmada:** En `src/main/resources/templates/traslados.html` se sustituyo la dependencia del visor nativo por renderizado del PDF real con `pdf.js` sobre canvas. Para el zoom, se guarda el PDF como `Blob` y se regeneran bytes frescos en cada rerender antes de llamar a `pdf.js`.
**Precaucion:** Si un PDF inline falla pero el endpoint devuelve `200`, `Content-Type: application/pdf` y bytes validos, NO tocar backend por inercia: validar primero si el visor nativo del navegador es el que falla. Y con `pdf.js`, no reutilizar el mismo `Uint8Array`/`ArrayBuffer` entre renders del worker.

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
- [ ] 19.11 Replantear la usabilidad real del modulo Documentos
       - El formulario de alta NO puede ser unico para todos los tipos de documento
       - Cada tipo (DI, NP, contrato, archivo cronologico, FA, etc.) debe mostrar sus propios campos
       - Los campos visibles y obligatorios deben cambiar segun `TipoDocumento`
- [ ] 19.12 Flujo guiado de creacion + adjunto PDF en la misma experiencia
       - Al enviar el formulario de creacion debe abrirse el paso de subida del PDF o incorporarse en el mismo wizard
       - Si el documento requiere PDF, no puede quedar en un estado ambiguo donde "no pasa nada"
       - El usuario debe recibir feedback claro: creado, pendiente de adjunto, adjuntado o error
- [ ] 19.13 Consistencia entre creacion, persistencia y tabla
       - Tras crear un documento debe aparecer inmediatamente en la lista `/documentos`
       - El alta no puede depender de un refresco manual ni de una subida posterior silenciosa para "existir"
       - Si falla la subida del PDF, el sistema debe mostrar el error y dejar un estado recuperable (`BORRADOR` o `PENDIENTE_ADJUNTO`)
- [ ] 19.14 Formularios especializados por tipo documental
       - DI: datos de traslado, origen, destino, transportista, cantidades, fechas
       - NP: fechas previstas, antelacion legal, residuos y agentes afectados
       - Contrato / FA / otros: campos legales y de referencia propios, no un formulario generico reciclado

**Rediseño propuesto del modulo Documentos (pendiente de ejecucion):**

**Diagnostico confirmado sobre la implementacion actual:**
- La vista actual `documents.html` usa un unico modal con los mismos campos para todos los `TipoDocumento`.
- El alta real depende de dos pasos desconectados: primero `POST /api/documentos`, despues subida manual del PDF.
- El usuario no percibe un estado transaccional claro: puede guardar metadatos, pero si no adjunta PDF el documento queda en una situacion ambigua.
- La generacion PDF por tipo esta acoplada de forma provisional en `DocumentoController`: varios tipos distintos reutilizan la misma salida base aunque por dominio no deberian compartir formulario ni plantilla.

**Implementacion objetivo (MVP serio, no parche):**

1. **Selector de tipo como primer paso del alta**
       - Al pulsar "Nuevo Documento", primero se elige `TipoDocumento`.
       - Ese selector gobierna el formulario que aparece despues, no solo una lista `select` dentro de un modal generico.

2. **Formularios por tipo documental**
       - **DI**: asociado obligatoriamente a un traslado; campos de referencia, fechas, observaciones y datos calculados/arrastrados del traslado.
       - **NP**: traslado o recogida prevista, fecha objetivo, fecha limite legal, agentes implicados, observaciones de cumplimiento.
       - **Contrato**: contraparte, vigencia, referencias legales, centro/empresa, observaciones.
       - **Ficha de aceptacion / Hoja de seguimiento / Informe final**: formularios especificos o al menos variantes con validaciones propias.
       - **Archivo cronologico**: no debe crearse con el mismo flujo manual si su naturaleza es automatica; debe tratarse como documento derivado del sistema.

3. **Wizard de alta real en 2 pasos**
       - **Paso 1:** crear metadatos del documento.
       - **Paso 2:** adjuntar PDF inmediatamente, en el mismo flujo visual.
       - Resultado visible al finalizar: documento persistido y fila actualizada en tabla con estado y acciones disponibles.

4. **Estados funcionales explicitos**
       - Introducir semantica de flujo: `BORRADOR`, `PENDIENTE_ADJUNTO`, `EMITIDO`, `CERRADO`, `VENCIDO`.
       - Si no se sube PDF, el documento debe quedar visible como `PENDIENTE_ADJUNTO`, no desaparecer del mapa mental del usuario.
       - La tabla `/documentos` debe permitir filtrar y resolver facilmente los documentos pendientes de adjunto.

5. **Persistencia y feedback inmediatos**
       - Tras `POST /api/documentos`, la nueva fila debe insertarse/mostrarse inmediatamente.
       - Si la subida del PDF falla, mostrar error accionable y mantener el documento creado con CTA clara para reintentar.
       - El alta no puede depender de un refresco posterior ni de que el usuario intuya que debe usar un icono de upload en otro momento.

6. **Separacion entre documentos generados y documentos subidos**
       - **Generados por el sistema**: DI, NP, certificados, archivo cronologico cuando proceda.
       - **Subidos externamente**: contratos firmados, fichas, anexos, versiones selladas.
       - El formulario debe dejar claro si se va a generar un PDF desde datos estructurados o si se espera la subida de un PDF externo.

7. **Backend a introducir para soportar la UX**
       - DTO de alta por tipo o DTO base + payload especializado por `TipoDocumento`.
       - Endpoint explicito de creacion de borrador/documento inicial.
       - Endpoint de adjunto desacoplado pero enlazado al flujo (`POST /api/documentos/{id}/archivo` o unificado), con respuesta rica para refrescar tabla.
       - Validaciones por tipo en backend, no solo ocultacion de campos en frontend.

8. **Criterio de aceptacion funcional**
       - Crear un documento debe producir una entidad visible en la tabla en todos los casos de exito parcial o total.
       - Cada tipo documental debe tener al menos un formulario distinguible y validado segun su dominio.
       - El usuario debe entender en todo momento si el documento fue creado, generado, adjuntado o quedo pendiente de completar.

**Contrato backend propuesto (primera iteracion):**

**DTO base de creacion manual:**
```java
DocumentoDraftCreateDTO {
       TipoDocumento tipo;
       Long trasladoId;      // requerido para DI y NP si nacen de traslado
       Long centroId;        // requerido para contratos u otros documentos de centro
       String numeroReferencia;
       LocalDate fechaEmision;
       LocalDate fechaVencimiento;
       String observaciones;
       Map<String, Object> metadatos; // payload especializado por tipo en la primera iteracion
}
```

**Respuesta de creacion:**
```java
DocumentoWorkflowDTO {
       Long id;
       String codigo;
       TipoDocumento tipo;
       EstadoDocumento estado;   // puede salir como PENDIENTE_ADJUNTO
       String numeroReferencia;
       boolean requiereAdjunto;
       boolean tieneArchivo;
       String archivoUrl;
       String siguienteAccion;   // "SUBIR_PDF", "GENERAR_PDF", "LISTO"
}
```

**Endpoints propuestos para soportar la UX:**
- `POST /api/documentos/drafts`
       Crea el documento base y devuelve `DocumentoWorkflowDTO`.
- `POST /api/documentos/{id}/archivo`
       Adjunta PDF a un documento ya creado y devuelve `DocumentoWorkflowDTO` actualizado.
- `POST /api/documentos/{id}/generar`
       Genera el PDF server-side para los tipos que nacen del dominio y devuelve estado final + URL.
- `GET /api/documentos/{id}/workflow`
       Recupera el estado actual del flujo para reintentos o recarga de pantalla.

**Reglas de negocio minimas por tipo (primera ola):**
- `DOCUMENTO_IDENTIFICACION`: requiere `trasladoId`; puede generarse desde datos del traslado.
- `NOTIFICACION_PREVIA`: requiere `trasladoId` o futura `recogidaId`; debe validar ventana temporal legal.
- `CONTRATO`: requiere `centroId` o entidad contraparte; normalmente espera PDF externo.
- `ARCHIVO_CRONOLOGICO`: no se crea manualmente desde el alta general.

**Migracion / compatibilidad de datos existentes:**
- Todos los documentos actuales con `archivoUrl = null` y estado `BORRADOR` pueden reinterpretarse como candidatos a `PENDIENTE_ADJUNTO`.
- No es obligatorio migrar historicamente todos los registros en la primera entrega si la UI ya resuelve ambos estados correctamente.
- La tabla debe mostrar de forma distinguible `BORRADOR` vs `PENDIENTE_ADJUNTO` para no mezclar documentos incompletos con borradores reales.

**Validacion minima a automatizar cuando se implemente:**
- Crear `CONTRATO` sin `centroId` debe fallar con error de validacion.
- Crear `DOCUMENTO_IDENTIFICACION` sin `trasladoId` debe fallar.
- Crear documento manual correcto debe devolver fila visible y estado coherente.
- Fallo en upload no debe borrar ni ocultar el documento creado.
- Upload correcto debe reflejar `archivoUrl` y quitar el estado pendiente si aplica.

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

### FASE 22 — Numeracion universal de registros (codigos legibles)

**Objetivo:** Sustituir la exposicion del `id` numerico en la UI por codigos legibles, trazables
y con formato estandar. El `id` interno sigue siendo la PK de la BD; el `codigo` es un campo
String unico adicional generado en `@PrePersist`.

**Formato:** `[3 letras][2 digitos ano]-[6 digitos correlativos]`
```
TRA26-000001   ← Traslado
RUT26-000001   ← Ruta
CEN26-000001   ← Centro
RES26-000001   ← Residuo
DOC26-000001   ← Documento
REC26-000001   ← Recogida
USU26-000001   ← Usuario
DIR26-000001   ← Direccion
EMP26-000001   ← Empresa
```

**Modelo de soporte:**
```java
// Tabla de secuencias por prefijo (evita colisiones en caso de borrado+reinsercion)
SecuenciaCodigo {
  String prefijo;   // "TRA", "RUT", etc. (PK)
  Long ultimo;      // ultimo numero usado
}

// Servicio centralizado
CodigoService {
  String generar(String prefijo); // incrementa atomicamente y devuelve "TRA26-000001"
}
```

**Cambios en entidades existentes:** anadir campo `String codigo` (`@Column(unique=true)`)
con `@PrePersist` que llama a `CodigoService`. El campo es de solo lectura tras la creacion.

**DataInitializer:** regenerar codigos para los registros de seed (todos existentes quedan con
codigo asignado desde la primera arrancada post-migracion).

**Tareas:**
- [ ] 22.1 Crear entidad `SecuenciaCodigo` y `CodigoService` (atomico, con `@Transactional`)
- [ ] 22.2 Anadir campo `codigo` a: `Traslado`, `Centro`, `Residuo`, `Documento`, `Recogida`, `Usuario`, `Direccion`, `Empresa`
- [ ] 22.3 `@PrePersist` en cada entidad llama a `CodigoService.generar(prefijo)`
- [ ] 22.4 Mostrar `codigo` en lugar de `id` en todas las tablas de la UI
- [ ] 22.5 Busqueda por `codigo` en todos los buscadores existentes
- [ ] 22.6 Seed: `DataInitializer` asigna `codigo` a los registros iniciales
- [ ] 22.7 Tests: `CodigoService` (unicidad, formato, concurrencia basica)

---

### FASE 23 — Perfil Transportista y sistema de tarifas

**Objetivo:** Ampliar el rol `TRANSPORTISTA` con datos profesionales (matricula, tarifa),
sin tocar la entidad `Usuario`. Patron: `OneToOne` opcional solo para transportistas.

**Modelo de datos:**
```java
// Nueva entidad — solo para usuarios con rol TRANSPORTISTA
PerfilTransportista {
  Long id;
  String codigo;             // TRP26-000001
  Usuario usuario;           // @OneToOne, unique
  String matricula;          // matricula del vehiculo principal
  String formulaTarifa;      // ej: "w * 0.5 + D * 0.1"
                             // variables permitidas: w (peso kg), D (distancia km)
                             // solo digitos, +-*/(), w, D, espacios
  String unidadTarifa;       // "EUR/operacion" (calculado desde formula)
  String observaciones;
}
```

**Validacion de formula (backend):**
- Libreria: `exp4j` (net.objecthunter:exp4j:0.4.8) — evaluador de expresiones matematicas, sin reflection, sin eval
- Regex de whitelist antes de persistir: `^[0-9wD\s\+\-\*/\(\)\.]+$`
- Test de evaluacion con `w=1, D=1` antes de guardar (si falla → error de validacion 400)

**Calculadora de precios (frontend):**
- Inputs: `w` (kg) y/o `D` (km) segun variables presentes en la formula
- Calculo en tiempo real: `GET /api/transportistas/{id}/calcular-tarifa?w=100&d=50` → `{"resultado": 55.00, "moneda": "EUR"}`
- Muestra detalle: "Tarifa aplicada: w * 0.5 + D * 0.1 con w=100kg, D=50km = 55.00 EUR"

**Adjuntos de usuario (extender `Adjunto` existente):**
La entidad `Adjunto` ya existe. Se extiende para soportar FK a `Usuario` (nullable),
permitiendo subir documentos a cualquier usuario (licencia transporte, contrato, DNI, etc.)

**Tareas:**
- [ ] 23.1 Crear entidad `PerfilTransportista` con repositorio y servicio
- [ ] 23.2 Anadir dependencia `exp4j` al `pom.xml`
- [ ] 23.3 `TarifaValidator`: valida formula con regex + evaluacion test
- [ ] 23.4 Vista `/usuarios/{id}` (perfil publico): si rol=TRANSPORTISTA, mostrar tab "Datos Transportista"
       - Formulario: matricula, formula tarifa, observaciones
       - Preview de calculo en tiempo real (JS)
- [ ] 23.5 Endpoint `GET /api/transportistas/{id}/calcular-tarifa?w=&d=` (calcula y devuelve resultado)
- [ ] 23.6 Adjuntos para usuarios: anadir FK `usuario` nullable a entidad `Adjunto` existente
       - Endpoint: `POST /api/usuarios/{id}/adjuntos` (subir documento)
       - Vista en perfil de usuario: tab "Documentos" con lista de adjuntos + upload
- [ ] 23.7 Tests: `TarifaValidator` (formulas validas, invalidas, inyeccion), calculo correcto

---

### FASE 24 — Modulo Rutas (mapa + tarifas + calculadora)

**Objetivo:** Vista completa de rutas: mapa con traslados activos, tab de rutas registradas
por transportistas con calculadora de precios, buscador y filtros.

**Contexto de mapa (decision tecnica):**
Google Maps API requiere tarjeta de credito y cobra por request. Se usa la alternativa
open source equivalente:
- **Leaflet.js** (ya en el proyecto) — motor de mapa interactivo
- **Leaflet Routing Machine** (plugin MIT) — dibuja rutas reales siguiendo carreteras
- **OSRM demo server** (`https://router.project-osrm.org`) — routing gratuito con datos
  OpenStreetMap, sin API key, adecuado para desarrollo y proyectos academicos
- Migracion futura a produccion: OpenRouteService API (2.000 req/dia gratis con API key)
  o self-hosted OSRM

**Modelo de datos nuevo:**
```java
Ruta {
  Long id;
  String codigo;                     // RUT26-000001
  String nombre;                     // ej: "Norte → Centro Reciclaje Sur"
  PerfilTransportista transportista; // @ManyToOne
  Direccion origen;                  // @ManyToOne (del catalogo de Direcciones)
  Direccion destino;                 // @ManyToOne (del catalogo de Direcciones)
  Double distanciaKm;                // calculada por OSRM o introducida manualmente
  String formulaTarifa;              // puede sobreescribir la del transportista para esta ruta
  String observaciones;
}
```

`Traslado` gana campo `Ruta ruta` (`@ManyToOne`, nullable — no rompe datos existentes).

**Vista `/rutas` — estructura:**
```
[Filtros: rango de fechas] [Buscador: transportista / nombre ruta / nº traslado]

MAPA (vista por defecto)
  ├── Rutas de traslados ACTIVOS dibujadas con Leaflet Routing Machine (linea real en carretera)
  ├── Marcadores: origen (verde) / destino (rojo) / transportista (azul)
  ├── Click en ruta → popup con: nº traslado, transportista, estado, fecha
  └── Buscador y filtros actualizan el mapa en tiempo real (HTMX o fetch)

TAB "Rutas y Tarifas"
  ├── Tabla de rutas registradas por transportistas (nombre, transportista, origen→destino, km, tarifa)
  ├── Click en fila → panel lateral: calculadora de precios
  │     ├── Formula aplicada: "w * 0.5 + D * 0.1"
  │     ├── Input w (kg) y/o D (km) segun variables en formula
  │     ├── Resultado en tiempo real: "Estimacion: 55.00 EUR"
  │     └── Boton "Asignar a traslado" (abre selector de traslado abierto)
  └── Boton "Nueva Ruta" (solo TRANSPORTISTA y ADMIN)
```

**Tareas:**
- [x] 24.1 Crear entidad `Ruta` con repositorio y servicio (`RutaService`)
- [x] 24.2 Anadir FK `ruta` a entidad `Traslado` (nullable, `@ManyToOne`)
- [x] 24.3 `RutaController`: CRUD + endpoint `GET /api/rutas/activas` (rutas de traslados activos)
- [x] 24.4 Integrar Leaflet Routing Machine via CDN en la vista `/rutas`
       ```html
       <link rel="stylesheet" href="https://unpkg.com/leaflet-routing-machine@3.2.12/dist/leaflet-routing-machine.css"/>
       <script src="https://unpkg.com/leaflet-routing-machine@3.2.12/dist/leaflet-routing-machine.min.js"></script>
       ```
- [x] 24.5 JavaScript: para cada ruta activa, crear `L.Routing.control` con origen/destino (lat/lon de `Direccion`)
       usando OSRM: `router: L.Routing.osrmv1({ serviceUrl: 'https://router.project-osrm.org/route/v1' })`
- [x] 24.6 Buscador y filtros de fecha → `GET /api/rutas/activas?desde=&hasta=&q=` (filtra por transportista, nombre, codigo traslado)
- [x] 24.7 Tab "Rutas y Tarifas": tabla + calculadora lateral con calculo en tiempo real
- [x] 24.8 Asignar ruta a traslado desde la calculadora (HTMX PATCH)
- [x] 24.9 En vista de traslado (detalle): mostrar ruta asignada con mini-mapa embebido
- [ ] 24.10 Tests: `RutaService`, calculo de tarifa en ruta

---

### FASE 25 — QR funcional: marcar entrada de traslado

**Objetivo:** El QR que ya se genera por traslado codifica el `codigo` del traslado (`TRA26-000001`).
Al escanearlo con la app movil (o la camara del navegador), se marca automaticamente la
entrada del traslado (cambio de estado a `EN_TRANSITO` o el que corresponda segun la maquina de estados).

**Requisito previo:** FASE 22 completada (el traslado debe tener `codigo` antes de generar el QR).

**Flujo:**
```
Transportista escanea QR (fisico o en pantalla)
       ↓
Navegador / App Android abre: /qr/entrada?codigo=TRA26-000001
       ↓
Backend valida: ¿existe el traslado? ¿esta en estado PENDIENTE?
       ↓
Cambia estado → EN_TRANSITO + crea EventoTraslado ("Entrada registrada via QR")
       ↓
Respuesta JSON: { "ok": true, "traslado": "TRA26-000001", "nuevoEstado": "EN_TRANSITO" }
```

**Generacion del QR (actualizar `QrController` existente):**
- Contenido del QR: URL completa `https://{host}/qr/entrada?codigo=TRA26-000001`
- (Antes contenia solo el ID numerico — actualizar para usar `codigo`)

**Tareas:**
- [x] 25.1 Actualizar `QrController`: generar QR con URL `{host}/qr/entrada?id={id}` (id numerico; codigo alfanumerico pendiente de FASE 22)
- [x] 25.2 Endpoint `GET /qr/entrada?id={id}`:
       - Valida existencia y estado del traslado
       - Cambia estado a `EN_TRANSITO` (si estaba PENDIENTE)
       - Crea `EventoTraslado` con comentario "Entrada registrada via QR"
       - Renderiza vista HTML de confirmacion o error
- [x] 25.3 Vista `/qr/confirmacion`: pagina responsive, muestra traslado, estado, botones
- [x] 25.4 Pagina `/qr/scanner` con camara (WebRTC + html5-qrcode CDN), autoarranque
- [x] 25.5 Tests: entrada con PENDIENTE (exito), EN_TRANSITO (excepcion), CANCELADO (excepcion), id inexistente

---

---

### FASE 26 — Tarifa por transportista en ruta (modelo real M:N)

**Objetivo:** Modelar correctamente la realidad del negocio: una ruta puede ser cubierta por
varios transportistas, cada uno con su propia tarifa. Un traslado ocurre en una ruta concreta,
y solo pueden asignarse transportistas que cubran esa ruta. El precio final se calcula con
la formula del transportista para esa ruta + el peso del residuo del traslado.

**Problema con el modelo actual:**
La entidad `Ruta` tiene un campo `transportista` (ManyToOne) → solo admite un transportista
por ruta. Esto es incorrecto. La relacion real es M:N con atributos propios (la tarifa), lo
que requiere una entidad join explícita.

**Nuevo modelo de datos:**

```
Ruta ──────────────────────────────────── RutaTransportista ─────────────────── Usuario (TRANSPORTISTA)
 id, nombre, origen, destino,              id                                     id, nombre, email
 distanciaKm, estado, coords              ruta       @ManyToOne → Ruta
 formulaTarifaBase (opcional,             transportista @ManyToOne → Usuario
   fallback si el transportista           formulaTarifa  TEXT  (ej: "w * 0.4 + L * 0.08")
   no tiene formula propia)              unidadTarifa   VARCHAR (EUR)
                                          activo         BOOLEAN
                                          UNIQUE(ruta_id, transportista_id)
```

Cambios en `Ruta`:
- El campo `transportista` (FK unica) pasa a ser `responsable` (opcional, solo administrativo)
  o se elimina. Los transportistas reales se gestionan via `RutaTransportista`.
- Se añade relacion `@OneToMany(mappedBy="ruta") List<RutaTransportista> asignaciones`
- `formulaTarifa` se convierte en tarifa base (fallback cuando el transportista no tiene formula propia)

Cambios en `Traslado`:
- Validacion: si `ruta` no es null, el `transportista` asignado DEBE existir en `RutaTransportista`
  para esa ruta. Error 400 si no cumple.

**Reglas de negocio:**
1. Solo ADMIN/GESTOR pueden asignar o quitar transportistas de una ruta.
2. Un transportista puede ver en su perfil las rutas que opera y sus tarifas.
3. Al crear un traslado con ruta asignada, el selector de transportistas muestra solo los
   de esa ruta (con su tarifa indicada como ayuda visual).
4. El precio estimado de un traslado se calcula como:
   `formula_transportista_ruta(w=residuo.cantidad, L=ruta.distanciaKm)`
   Si el transportista no tiene formula propia en esa ruta: usar `ruta.formulaTarifaBase`.
5. Si no hay formula en ningún nivel: mostrar "Sin tarifa definida".

**API nueva:**

| Metodo | Ruta                                            | Acceso          | Descripcion                                      |
|--------|-------------------------------------------------|-----------------|--------------------------------------------------|
| GET    | /api/rutas/{id}/transportistas                  | AUTH            | Lista transportistas de la ruta con su tarifa    |
| POST   | /api/rutas/{id}/transportistas                  | ADMIN/GESTOR    | Asigna transportista a ruta con su formula       |
| PUT    | /api/rutas/{id}/transportistas/{transId}        | ADMIN/GESTOR    | Actualiza formula/moneda del transportista       |
| DELETE | /api/rutas/{id}/transportistas/{transId}        | ADMIN/GESTOR    | Desvincula transportista de la ruta              |
| GET    | /api/rutas/{id}/calcular/{transId}?w=           | AUTH            | Precio para transportista especifico + peso      |
| GET    | /api/traslados/transportistas-por-ruta?rutaId=  | AUTH            | Transportistas elegibles para un traslado        |

**Frontend — cambios en `/routes`:**
- Tab "Transportistas" en la pagina de rutas (o panel lateral al seleccionar una ruta en tab Lista):
  - Tabla: Nombre transportista | Formula | Moneda | Precio estimado (con w=100 kg por defecto)
  - Boton "Añadir transportista" (ADMIN/GESTOR) → mini-modal con selector + formula
  - Boton "Quitar" por fila (ADMIN/GESTOR)
  - Formula editable inline (ADMIN/GESTOR)

**Frontend — cambios en formulario de traslados (`shipments.html`):**
- Al seleccionar/cambiar una ruta en el formulario de traslado:
  - Selector de transportista se recarga via `GET /api/traslados/transportistas-por-ruta?rutaId=X`
  - Cada opcion muestra el nombre + tarifa estimada calculada en tiempo real
  - Si no hay transportistas en la ruta: aviso "Esta ruta aun no tiene transportistas asignados"

**Tareas:**
- [x] 26.1 Entidad `RutaTransportista` con repositorio y constraint UNIQUE(ruta_id, transportista_id)
- [x] 26.2 Modificar `Ruta`: añadir `@OneToMany asignaciones`, mantener `formulaTarifaBase` como fallback
- [x] 26.3 `RutaTransportista` service + endpoints CRUD (`/api/rutas/{id}/transportistas`)
- [x] 26.4 Endpoint `GET /api/rutas/{id}/calcular/{transId}?w=`: usa formula del transportista,
       cae al fallback de la ruta si no tiene formula propia
- [x] 26.5 Endpoint `GET /api/rutas/{id}/transportistas`: lista elegibles para el formulario de traslado
- [x] 26.6 Validacion en `TrasladoService`: al asignar transportista + ruta, verificar que
       el transportista pertenece a esa ruta (lanza 400 si no)
- [x] 26.7 Frontend `/routes` — tab o panel "Transportistas por ruta":
       tabla de asignaciones, formulario de alta/baja, precio estimado en tiempo real
- [x] 26.8 Frontend formulario traslado: selector de transportistas filtrado por ruta,
       precio estimado del traslado mostrado mientras se introduce el formulario
- [x] 26.9 Tests: `RutaTransportista` CRUD, calculo con formula propia vs fallback,
       validacion de transportista en traslado

**Diagrama de flujo (creacion de traslado con ruta):**
```
ADMIN/GESTOR crea traslado
       ├── Selecciona ruta
       │       ↓
       │   API devuelve transportistas asignados a esa ruta (con tarifa)
       │       ↓
       ├── Selecciona transportista (del subconjunto filtrado)
       │       ↓
       │   UI muestra: "Precio estimado: 87.50 EUR (formula: w*0.4 + L*0.08, w=200kg, L=145km)"
       │       ↓
       └── Guarda traslado → backend valida ruta+transportista coherentes
```

**Dependencias:** FASE 24 completada (entidad Ruta con coords y formula base ya existen).

---

### FASE 27 — Registro de usuarios, notificaciones y gestion de solicitudes

**Objetivo:** Implementar el flujo completo de alta de nuevos usuarios:
un visitante solicita registrarse, el admin recibe notificacion, revisa, aprueba o rechaza,
y establece la contrasena del nuevo usuario. Ampliar la gestion de usuarios del admin para
que pueda ver perfiles y cambiar contrasenas de cualquier usuario.

**Flujo de registro:**
```
Visitante (no autenticado)
       │
       ├── En /login pulsa "Registrarse"
       │       ↓
       ├── /registro — selecciona rol: Productor / Gestor / Transportista
       │       ↓
       ├── Formulario dinamico (campos segun rol seleccionado):
       │     Comunes:     nombre, email, telefono, DNI
       │     Productor:   empresa, NIMA, centro principal
       │     Gestor:      empresa, autorizacion gestor, NIMA
       │     Transportista: matricula, certificado ADR
       │       ↓
       └── Enviar solicitud → se guarda en tabla SolicitudRegistro (estado=PENDIENTE)
                ↓
       Sistema genera Notificacion para ADMIN ("Nueva solicitud de registro")
                ↓
       ADMIN ve campanita con contador en header
                ↓
       ADMIN abre /solicitudes → lista de solicitudes pendientes
                ↓
       ADMIN revisa datos → Acepta (crea Usuario + establece contrasena) o Rechaza
```

**Modelo de datos nuevo:**

```java
// Nueva entidad — solicitud de registro pendiente de aprobacion
SolicitudRegistro {
  Long id;
  String nombre;
  String email;
  String telefono;
  String dni;
  Rol rolSolicitado;           // PRODUCTOR, GESTOR, TRANSPORTISTA (nunca ADMIN)
  // Campos especificos segun rol (todos nullable)
  String empresa;              // nombre de empresa (Productor/Gestor)
  String nima;                 // NIMA (Productor/Gestor)
  String autorizacionGestor;   // ref autorizacion (Gestor)
  String matricula;            // matricula vehiculo (Transportista)
  String certificadoAdr;       // ref certificado ADR (Transportista)
  String centroPrincipal;      // nombre centro (Productor)
  EstadoSolicitud estado;      // PENDIENTE, APROBADA, RECHAZADA
  String motivoRechazo;        // texto libre si se rechaza
  LocalDateTime fechaSolicitud;
  LocalDateTime fechaResolucion;
  Usuario resueltoPor;         // FK al admin que resolvio
}

enum EstadoSolicitud {
  PENDIENTE, APROBADA, RECHAZADA
}

// Nueva entidad — notificaciones genericas
Notificacion {
  Long id;
  Usuario destinatario;        // FK al usuario que la recibe
  String titulo;               // ej: "Nueva solicitud de registro"
  String mensaje;              // ej: "Juan Perez solicita registrarse como Productor"
  String enlace;               // ej: "/solicitudes/5"
  Boolean leida;               // false por defecto
  LocalDateTime fecha;
}
```

**Tareas:**

- [ ] 27.1 Crear entidad `SolicitudRegistro` + `EstadoSolicitud` (enum) + repositorio + servicio
- [ ] 27.2 Crear entidad `Notificacion` + repositorio + servicio
- [ ] 27.3 Pagina `/registro` (publica, sin autenticar):
       - Selector de rol (3 tarjetas: Productor / Gestor / Transportista)
       - Formulario dinamico: campos comunes siempre visibles, campos por rol aparecen/ocultan con JS
       - Validacion frontend (campos obligatorios) + backend (email unico, DNI formato)
       - Boton "Enviar solicitud" → POST /api/solicitudes-registro
       - Mensaje de confirmacion: "Tu solicitud ha sido enviada. Un administrador la revisara."
- [ ] 27.4 Enlace "Registrarse" en pagina de login
- [ ] 27.5 Campanita de notificaciones en header del layout:
       - Icono campana con badge numerico (notificaciones no leidas)
       - Dropdown al hacer clic: lista de notificaciones recientes
       - Clic en notificacion → navega al enlace y marca como leida
       - Endpoint: `GET /api/notificaciones` (del usuario autenticado)
       - Endpoint: `PATCH /api/notificaciones/{id}/leer`
- [ ] 27.6 Pagina `/solicitudes` (solo ADMIN):
       - Tabla: nombre, email, rol solicitado, fecha, estado
       - Filtro por estado (Pendiente / Aprobada / Rechazada)
       - Clic en fila → detalle con todos los campos del formulario
- [ ] 27.7 Flujo de aprobacion (ADMIN):
       - Boton "Aprobar" → formulario con campo contrasena (que el admin establece)
       - Al aprobar: crea Usuario con datos de la solicitud, encripta contrasena, asigna rol
       - Si rol=TRANSPORTISTA: crea tambien PerfilTransportista con matricula
       - Cambia estado solicitud a APROBADA
       - Genera notificacion (futuro: email al solicitante informando que fue aprobado)
- [ ] 27.8 Flujo de rechazo (ADMIN):
       - Boton "Rechazar" → campo texto para motivo
       - Cambia estado solicitud a RECHAZADA
- [ ] 27.9 Ampliar gestion de usuarios (ADMIN):
       - Ver perfil completo de cualquier usuario (no solo lista)
       - Cambiar contrasena de cualquier usuario (formulario en su perfil)
       - Asignar/cambiar rol de un usuario existente
- [ ] 27.10 Seguridad:
       - `/registro` y `POST /api/solicitudes-registro` son publicos (como /login)
       - `/solicitudes` y endpoints de aprobacion/rechazo requieren ROLE_ADMIN
       - Notificaciones: cada usuario solo ve las suyas
       - Rate limiting basico en registro (evitar spam de solicitudes)
- [ ] 27.11 API endpoints:
       - `POST /api/solicitudes-registro` (publico — crea solicitud)
       - `GET /api/solicitudes-registro` (ADMIN — lista con filtros)
       - `GET /api/solicitudes-registro/{id}` (ADMIN — detalle)
       - `POST /api/solicitudes-registro/{id}/aprobar` (ADMIN — aprueba y crea usuario)
       - `POST /api/solicitudes-registro/{id}/rechazar` (ADMIN — rechaza con motivo)
       - `GET /api/notificaciones` (autenticado — mis notificaciones)
       - `PATCH /api/notificaciones/{id}/leer` (autenticado — marcar leida)
       - `GET /api/notificaciones/no-leidas` (autenticado — contador para badge)
- [ ] 27.12 Tests: registro, aprobacion, rechazo, notificaciones, seguridad endpoints publicos

**Diagramas de casos de uso:** Ver archivos separados por actor:
- `uc-visitante.puml` — flujo de registro
- `uc-admin.puml` — notificaciones + solicitudes + gestion usuarios ampliada
- `uc-productor.puml`
- `uc-gestor.puml`
- `uc-transportista.puml`

**Dependencias:** Ninguna estricta. Puede ejecutarse en paralelo con fases 20+.
Si FASE 23 (PerfilTransportista) esta completada, al aprobar un transportista se crea
automaticamente el perfil. Si no, se crea el usuario sin perfil y se completa despues.

---

### FASE 28 — Permisos por propiedad (ownership) y filtrado de datos por rol

**Objetivo:** Cada usuario solo ve y opera sobre los datos que le corresponden. El Admin ve
todo, pero Gestor, Productor y Transportista trabajan en un universo restringido a sus centros,
residuos, traslados y recogidas. Esto se implementa con tablas de relacion M:N de ownership
y filtrado en servicios, sin framework externo (Spring Security ACL anade complejidad
innecesaria para este caso; la solucion custom con JPA Specifications es mas simple, legible
y mantenible).

**Principio general:** si un dato no pertenece al usuario, no existe para el. No aparece en
selects, no se incluye en estadisticas, no se puede modificar ni consultar por URL directa.

**Modelo de ownership por rol:**

| Rol | Owns | Ve | Opera |
|-----|------|----|-------|
| **Admin** | Todo | Todo | Todo |
| **Gestor** | Centros asignados via `GestorCentro` | Centros propios + sus residuos, traslados, recogidas, documentos, estadisticas | CRUD completo en su ambito. Puede enviar recogidas A cualquier centro (centroDestino), pero solo crear recogidas DESDE sus centros (centroOrigen) |
| **Productor** | Centros via `Centro.usuario` (ya existe) | Sus centros + sus residuos + recogidas de sus centros | Gestiona almacen (cantidades, entradas/salidas). Decide si se puede recoger o no y que cantidad. Ve traslados de sus centros como solo lectura. NO cambia estados de traslados. Su gestion termina al aceptar/rechazar la recogida |
| **Transportista** | Rutas/traslados asignados via `RutaTransportista` (ya existe) + campo `transportista` en Traslado/Recogida | Traslados y recogidas donde es transportista asignado. Rutas propias | Maximo poder sobre transporte: CRUD completo de rutas propias (trayecto completo, no solo inicio/fin), precios/tarifas, estados de traslado. Puede postergar fecha de recogida (solo a fecha posterior, nunca anterior). Ve centros SOLO en contexto de sus operaciones, no como listado general |

**Tecnologia elegida: Ownership Service + JPA Specifications (custom)**

Justificacion frente a alternativas:
- **Spring Security ACL**: potente pero pesado — requiere 4 tablas propias, configuracion
  compleja, curva de aprendizaje alta. Sobredimensionado para este caso donde el modelo
  de permisos es "ves lo tuyo y punto".
- **Row-Level Security (PostgreSQL)**: buena en produccion pero no funciona con H2 en
  desarrollo/tests. Acopla la logica de negocio a la base de datos.
- **Custom OwnershipService + Specifications** ✅: una capa de servicio que, dado el usuario
  actual y su rol, devuelve las Specifications JPA para filtrar queries. Simple, testeable,
  funciona con cualquier base de datos, y aprovecha lo que Spring Data JPA ya ofrece.

**Tecnologia elegida para UI dinamica: @JsonView + FieldPermissionService**

El sistema de permisos NO se limita a filtrar filas (que registros ves). Tambien controla
CAMPOS (que datos ves dentro de un registro) y ACCIONES (que botones/formularios tienes).
Principio: si no es para ti, no existe en tu pantalla. Nada de errores 403, tablas vacias
ni campos deshabilitados sin sentido — directamente no se renderizan.

Opciones evaluadas para campo-level y UI dinamica:
- **DTOs por rol** (TrasladoAdminDTO, TrasladoProductorDTO...): seguro pero explosion de
  clases. 4 roles × N entidades = mantenimiento insostenible. Descartado.
- **GraphQL**: el cliente pide exactamente lo que necesita. Elegante, pero cambio de
  paradigma completo (dejar REST + Thymeleaf). Sobredimensionado para este proyecto.
- **@JsonView (Jackson)** ✅ para API: anotaciones en los campos del modelo que definen
  grupos de visibilidad (Views.Admin, Views.Gestor, Views.Productor, Views.Transportista).
  El controlador selecciona la vista segun el rol del usuario autenticado. Sin clases extra.
  Spring lo soporta nativamente con `@JsonView` en `@GetMapping`.
- **FieldPermissionService** ✅ para Thymeleaf: servicio que devuelve un mapa
  `Map<String, FieldPermission>` por entidad+rol+contexto. Cada campo tiene un estado:
  VISIBLE (lectura), EDITABLE (lectura+escritura), HIDDEN (no se renderiza).
  Thymeleaf lo consume via un objeto `permisos` en el modelo y fragmentos condicionales.
  Centralizado en un solo punto — NO disperso en decenas de `th:if="hasRole(...)"`

Capas del sistema de permisos (de abajo a arriba):
1. **Data-level**: JPA Specifications filtran queries → no ves registros ajenos
2. **Field-level**: @JsonView (API) + FieldPermissionService (Thymeleaf) → no ves campos ajenos
3. **Action-level**: OwnershipService valida escrituras + @PreAuthorize → no operas fuera de tu ambito
4. **UI-level**: formularios/modales/menus se renderizan segun FieldPermissionService → experiencia limpia
5. **Report-level**: cada rol tiene lista de informes permitidos → menu solo muestra los suyos

**Flujo de negociacion de fecha de recogida (validacion mutua):**

Una recogida no se ejecuta sin acuerdo entre las partes. Estados de confirmacion:

```
PROPUESTA → CONTRAPROPUESTA → CONFIRMADA_PRODUCTOR/CONFIRMADA_TRANSPORTISTA → ACORDADA
```

1. Productor o Gestor propone fecha de recogida (`fechaPropuesta`)
2. Transportista puede ACEPTAR o CONTRAPROPONER una fecha posterior (nunca anterior)
   - Si contrapropone, se guarda `fechaContrapropuesta` y el estado pasa a CONTRAPROPUESTA
3. Productor confirma la fecha final → `confirmacionProductor = true`
4. Transportista confirma → `confirmacionTransportista = true`
5. Cuando ambas confirmaciones son true → estado = ACORDADA, la recogida se puede ejecutar
6. El Gestor puede manipular/editar la recogida, pero si Productor o Transportista
   no dan su visto bueno, la recogida NO pasa a ACORDADA y no se ejecuta ese dia

Nuevos campos en `Recogida`:
- `fechaPropuesta` (LocalDate) — fecha original propuesta
- `fechaContrapropuesta` (LocalDate, nullable) — fecha alternativa del transportista
- `confirmacionProductor` (boolean, default false)
- `confirmacionTransportista` (boolean, default false)
- `estadoAcuerdo` (enum: PROPUESTA, CONTRAPROPUESTA, ACORDADA, RECHAZADA)

**Capas adicionales de filtrado del Gestor:**

Los gestores no solo estan limitados por centros. Tambien pueden tener:
- **Licencias por tipo de residuo**: un gestor solo trabaja con ciertos codigos LER.
  Necesita tabla `GestorLicencia` (gestor_id, codigoLER). Los residuos que no estan en
  sus licencias no aparecen en sus vistas ni en sus operaciones.
- **Empresas/productores asociados**: esto se resuelve de forma natural — si un gestor
  solo gestiona ciertos centros, y los centros pertenecen a productores especificos,
  el gestor solo ve las operaciones de esos productores. No necesita tabla extra.

**Tareas:**

- [ ] 28.1 Entidad `GestorCentro` (tabla M:N):
       - `gestor_id` (FK → Usuario, solo rol GESTOR)
       - `centro_id` (FK → Centro)
       - `fechaAsignacion` (audit)
       - Unique constraint: `(gestor_id, centro_id)`
       - Un centro puede tener multiples gestores; un gestor puede gestionar multiples centros
       - NOTA: Para Productor se reutiliza `Centro.usuario` (1:N existente, un productor
         es dueno de sus centros). Para Transportista se reutiliza `RutaTransportista` (ya M:N)
         y los campos `transportista` en Traslado/Recogida

- [ ] 28.2 Servicio `OwnershipService`:
       - `getCentrosPermitidos(usuario)` → segun rol:
         - ADMIN: todos
         - GESTOR: via GestorCentroRepository
         - PRODUCTOR: via CentroRepository.findByUsuario
         - TRANSPORTISTA: centros de sus traslados/recogidas asignados (no listado libre)
       - `canAccessCentro(usuario, centroId)` → booleano
       - `canCreateRecogidaDesde(usuario, centroOrigenId)` → GESTOR solo si es su centro;
         PRODUCTOR solo si es su centro; TRANSPORTISTA no crea recogidas
       - `canSendRecogidaA(usuario, centroDestinoId)` → GESTOR siempre true (puede enviar
         a cualquier centro); PRODUCTOR no envia
       - `getTrasladosPermitidos(usuario)` → filtrados por centros propios o asignacion directa
       - `getRecogidaPermitidas(usuario)` → idem

- [ ] 28.3 JPA Specifications para filtrado:
       - `CentroSpecifications.deUsuario(usuario)` → filtra por ownership segun rol
       - `ResiduoSpecifications.deUsuario(usuario)` → residuos de centros permitidos
       - `TrasladoSpecifications.deUsuario(usuario)` → traslados de centros permitidos O asignacion directa
       - `RecogidaSpecifications.deUsuario(usuario)` → recogidas de centros permitidos O asignacion directa
       - `DocumentoSpecifications.deUsuario(usuario)` → documentos de traslados/centros permitidos
       - Todos los Repositories deben extender `JpaSpecificationExecutor<T>`

- [ ] 28.4 Refactor de servicios existentes para usar Specifications:
       - `CentroService`: todas las queries filtran por ownership (listados, selects, busquedas)
       - `ResiduoService`: filtrar por centros del usuario
       - `TrasladoService`: filtrar por centros del usuario + asignacion como transportista
       - `RecogidaService`: filtrar por centros del usuario + asignacion como transportista
       - `DocumentoService`: filtrar por traslados/centros accesibles
       - `EstadisticaService` / Dashboard: SOLO datos de centros propios del usuario
       - Cada servicio inyecta `OwnershipService` y aplica Specification en el findAll

- [ ] 28.5 Restricciones de operacion por rol del Productor:
       - Traslados: SOLO lectura. Ve los traslados donde `centroProductor` es suyo.
         No puede crear, editar ni cambiar estado de traslados
       - Recogidas: puede ACEPTAR o RECHAZAR recogidas programadas para sus centros.
         Puede indicar cantidad disponible para recogida. No crea recogidas nuevas
       - Residuos: CRUD completo de residuos en sus centros (es su almacen).
         Gestiona cantidades, fechas de entrada/salida, alertas FIFO
       - Centros: ve y edita SOLO sus propios centros
       - Documentos: lectura de documentos asociados a sus traslados
       - Estadisticas: solo de sus centros (residuos en almacen, recogidas pendientes,
         traslados salientes)

- [ ] 28.6 Restricciones de operacion por rol del Transportista:
       - Traslados: ve SOLO los que tiene asignados. Cambia estados de transporte
         (es quien mas sabe de lo que pasa en el transporte: EN_TRANSITO, ENTREGADO, incidencias)
       - Recogidas: ve SOLO las que tiene asignadas. Puede postergar fecha de recogida
         (solo a fecha posterior, nunca anterior). Confirma recogida realizada
       - Rutas: CRUD COMPLETO de sus rutas propias — no solo inicio/fin, tambien trayecto
         intermedio (waypoints), tiempos estimados. No ve rutas de otros transportistas
       - Precios/tarifas: gestiona sus tarifas y costes de transporte
       - Centros: NO accede al listado general. Ve datos del centro solo en contexto
         de un traslado/recogida (direccion, contacto, para la logistica)
       - Residuos: NO accede directamente. Ve info del residuo dentro del traslado
       - Documentos: lectura de documentos de sus traslados asignados
       - Estadisticas: sus traslados realizados, tarifas acumuladas, rutas completadas

- [ ] 28.7 Restricciones de operacion por rol del Gestor:
       - Centros: CRUD de centros asignados via `GestorCentro`. No ve ni sabe que existen
         centros que no gestiona
       - Residuos: CRUD de residuos en sus centros asignados
       - Traslados: CRUD completo donde centroGestor o centroProductor sea de sus centros
       - Recogidas: puede crear recogidas DESDE sus centros (centroOrigen). Puede enviar
         recogidas A cualquier centro (centroDestino libre en el select)
       - Documentos: CRUD de documentos en traslados/centros de su ambito
       - Rutas: ve y asigna rutas (no limitado por ownership de rutas)
       - Estadisticas: solo datos de sus centros gestionados
       - Informes: generados solo con datos de su ambito

- [ ] 28.8 Filtrado en UI (selects y dropdowns):
       - Select de centros: solo muestra centros del usuario (segun ownership)
       - Select de centro destino en recogida (Gestor): muestra TODOS los centros
         (excepcion: puede enviar a cualquier centro)
       - Select de residuos: solo residuos de centros propios
       - Select de transportistas: sin restriccion (Gestor/Admin eligen transportista)
       - Dashboard widgets: datos solo del ambito del usuario
       - Tablas de listado: pre-filtradas por ownership, sin opcion de ver "todos"

- [ ] 28.9 Validacion en backend (defensa en profundidad):
       - Cada endpoint de escritura (POST, PUT, PATCH, DELETE) valida ownership
         ANTES de ejecutar la operacion. No basta con filtrar el listado
       - `@PreAuthorize` con SpEL para verificaciones rapidas de rol
       - Verificacion explicita en el servicio: `ownershipService.canAccessCentro(...)`
         lanza `AccessDeniedException` si no tiene permiso
       - Los endpoints GET de detalle (`/api/centros/{id}`) tambien verifican ownership
       - Los ID recibidos por URL no se confian: se valida que el recurso pertenece al usuario

- [ ] 28.10 Admin: gestion de asignaciones:
       - Pantalla para asignar/desasignar centros a gestores (CRUD de GestorCentro)
       - Vista de "que gestiona cada gestor" y "quien gestiona cada centro"
       - Al crear un centro, opcion de asignar gestor inmediatamente
       - Al desasignar un gestor de un centro, los traslados/recogidas en curso
         siguen visibles para el hasta completarse (no se corta el acceso en medio)

- [ ] 28.11 Migracion de datos existentes:
       - Script SQL / DataLoader que para cada Centro con `usuario` de rol GESTOR,
         crea la entrada correspondiente en `GestorCentro`
       - Verificar que Productores siguen funcionando con `Centro.usuario` sin cambios
       - Verificar que Transportistas siguen viendo traslados por campo `transportista`

- [ ] 28.12 Tests:
       - Test unitario de `OwnershipService` con cada rol
       - Test de Specifications: que los filtros devuelven solo datos propios
       - Test de integracion: Gestor A no ve centros de Gestor B
       - Test de integracion: Productor solo ve sus centros y residuos
       - Test de integracion: Productor NO puede cambiar estado de traslado (403)
       - Test de integracion: Transportista solo ve traslados asignados
       - Test de integracion: Gestor puede crear recogida a centro ajeno (centroDestino)
       - Test de seguridad: acceso directo por URL a recurso ajeno devuelve 403
       - Test de UI: selects solo muestran opciones permitidas

- [ ] 28.13 Entidad `GestorLicencia` (tabla M:N):
       - `gestor_id` (FK → Usuario, solo rol GESTOR)
       - `codigoLER` (String, codigo LER autorizado)
       - Unique constraint: `(gestor_id, codigoLER)`
       - El OwnershipService filtra residuos no solo por centro sino tambien por
         licencia LER del gestor. Si un gestor no tiene licencia para un LER,
         los residuos con ese codigo no aparecen en sus vistas

- [ ] 28.14 Flujo de negociacion de fecha de recogida:
       - Nuevos campos en Recogida: fechaPropuesta, fechaContrapropuesta,
         confirmacionProductor, confirmacionTransportista, estadoAcuerdo
       - Enum `EstadoAcuerdo`: PROPUESTA, CONTRAPROPUESTA, ACORDADA, RECHAZADA
       - Endpoint: `PATCH /api/recogidas/{id}/contraproponer` (TRANSPORTISTA — nueva fecha)
       - Endpoint: `PATCH /api/recogidas/{id}/confirmar` (PRODUCTOR o TRANSPORTISTA)
       - Validacion: contrapropuesta solo acepta fecha >= fechaPropuesta
       - Validacion: recogida no pasa a ACORDADA sin ambas confirmaciones
       - El Gestor puede editar la recogida pero no fuerza el estado ACORDADA
         sin visto bueno de las partes
       - Notificacion al cambiar estado de acuerdo (via sistema de notificaciones FASE 27)

- [ ] 28.15 FieldPermissionService (visibilidad de campos por rol):
       - Servicio central que dado (entidad, rol, contexto) devuelve
         `Map<String, FieldPermission>` donde FieldPermission = VISIBLE | EDITABLE | HIDDEN
       - Configuracion declarativa (no codigo disperso): un enum o mapa estatico define
         la matriz entidad × rol × campo → permiso
       - Ejemplo: campo `observacionesInternas` en Traslado → HIDDEN para Productor,
         VISIBLE para Transportista, EDITABLE para Gestor/Admin
       - El controlador anade `permisos` al Model de Thymeleaf
       - Fragmentos Thymeleaf reutilizables: `field-wrapper` que consulta el mapa
         y renderiza el campo como input, como texto read-only, o no lo renderiza

- [ ] 28.16 @JsonView para API REST:
       - Definir interfaces de vista: Views.Admin, Views.Gestor, Views.Productor,
         Views.Transportista, Views.Public
       - Anotar campos de las entidades/DTOs con las vistas correspondientes
       - En controladores REST: `@JsonView(Views.xxx.class)` segun rol del usuario
       - Resolver la vista dinamicamente con un `ViewResolverAdvice` que detecta
         el rol del usuario autenticado y aplica la vista correcta automaticamente

- [ ] 28.17 Informes filtrados por rol:
       - Definir lista de informes permitidos por rol (config declarativa)
       - ADMIN: todos los informes
       - GESTOR: informes de centros gestionados, inventario propio, trazabilidad propia
       - PRODUCTOR: informe de almacen, recogidas pendientes, traslados salientes
       - TRANSPORTISTA: informe de rutas, tarifas acumuladas, traslados realizados
       - Menu lateral de informes solo muestra los del rol del usuario
       - Los datos de cada informe se filtran por ownership (Specifications)

- [ ] 28.18 Tests adicionales UI dinamica:
       - Test: Productor no ve campo `observacionesInternas` en detalle de traslado
       - Test: Transportista ve boton "Postergar fecha" en recogida asignada
       - Test: flujo completo de negociacion de fecha (propuesta → contrapropuesta → acuerdo)
       - Test: Gestor sin licencia LER X no ve residuos con codigo LER X
       - Test: menu de informes del Productor solo muestra 3 opciones
       - Test: FieldPermissionService devuelve HIDDEN para campo no autorizado

**Dependencias:** Requiere que las entidades base existan (Centro, Traslado, Recogida,
Residuo, Documento). Idealmente ejecutar despues de FASE 18 (Recogida) y FASE 19 (Documento).
Compatible con cualquier fase de UI ya completada (los selects se refactorizan in-place).
FASE 23 (PerfilTransportista) y FASE 24 (Rutas) deben estar completas para el ownership
del transportista.

---

### Orden de ejecucion (Bloque II)

| Prioridad | Fase | Justificacion |
|-----------|------|---------------|
| 1 | 17 (navegacion) | Base visual para todo lo demas. Baja complejidad, alto impacto. |
| 2 | 18 (calendario + FIFO) | Feature diferencial. Requiere nuevas entidades (Recogida). |
| 3 | 19 (documentos) | Nucleo legal del producto. Bloquea los informes. |
| 4 | 20 (informes) | Depende de documentos y FIFO. Cierre del producto. |
| 5 | 21 (negocio) | Datos de empresa necesarios para PDFs correctos. |
| 6 | 22 (numeracion) | Cross-cutting. Debe hacerse antes de 23-25 para que todo lo nuevo use codigo. |
| 7 | 23 (transportista + tarifas) | Requiere 22 (codigo en entidades). |
| 8 | 24 (rutas + mapa) | Requiere 22 + 23 (PerfilTransportista existe). |
| 9 | 25 (QR funcional) | Requiere 22 (codigo en traslados). Cierre de trazabilidad. |
| 10 | 27 (registro + notificaciones) | Sin dependencias estrictas. Puede avanzar en paralelo desde fase 17+. |
| 11 | 28 (ownership + permisos datos) | Requiere 18, 19, 23, 24. Cross-cutting: afecta a todos los servicios. Ultima fase funcional. |

### Dependencias Bloque II

```
17 (nav) → independiente, puede ir primero
18 (FIFO + recogidas) → requiere 17 terminado en UI
19 (documentos) → requiere 18 (Recogida existe antes que Documento)
20 (informes) → requiere 18 + 19 (datos completos)
21 (negocio) → requiere 17, paralelo con 18
22 (codigos) → puede ir en paralelo con 17-21, pero BLOQUEA 23, 24, 25
23 (transportista) → requiere 22
24 (rutas) → requiere 22 + 23
25 (QR) → requiere 22
27 (registro + notificaciones) → independiente, paralelo desde 17+. Si 23 existe, crea perfil transportista al aprobar.
28 (ownership + permisos datos) → requiere 18 + 19 + 23 + 24. Refactoriza TODOS los servicios. Ejecutar al final del bloque.
```

---

### Resumen de nuevas entidades a crear (Bloque II)

| Entidad | Fase | Descripcion |
|---------|------|-------------|
| `Recogida` | 18 | Recogida programada con fechas, estado y transportista |
| `Documento` | 19 | Documento legal (DI, NP, Contrato, etc.) vinculado a traslado/recogida |
| `Empresa` | 21 | Datos legales de la empresa (singleton) |
| `SecuenciaCodigo` | 22 | Tabla de secuencias por prefijo para generacion de codigos unicos |
| `PerfilTransportista` | 23 | Datos profesionales del transportista (matricula, formula tarifa) |
| `Ruta` | 24 | Ruta entre dos Direcciones del catalogo, asignable a Traslado |
| Campos en `Residuo` | 18 | `fechaEntradaAlmacen`, `fechaSalidaAlmacen`, `diasMaximoAlmacenamiento` |
| Campo `ruta` en `Traslado` | 24 | FK nullable a `Ruta` |
| Campo `codigo` en todas | 22 | Codigo legible unico (`TRA26-000001`) sustituyendo ID en UI |
| `SolicitudRegistro` | 27 | Solicitud de registro pendiente de aprobacion admin |
| `EstadoSolicitud` | 27 | Enum: PENDIENTE, APROBADA, RECHAZADA |
| `Notificacion` | 27 | Notificacion generica por usuario (titulo, enlace, leida) |
| `GestorCentro` | 28 | Relacion M:N entre Gestor y Centro (ownership de gestion) |
| `GestorLicencia` | 28 | Relacion M:N entre Gestor y codigoLER (licencias de residuo autorizadas) |
| `EstadoAcuerdo` | 28 | Enum: PROPUESTA, CONTRAPROPUESTA, ACORDADA, RECHAZADA |
| Campos en `Recogida` | 28 | fechaPropuesta, fechaContrapropuesta, confirmacionProductor, confirmacionTransportista |
| Specifications JPA | 28 | Filtros dinamicos por ownership en cada entidad principal |
| `FieldPermissionService` | 28 | Servicio central de visibilidad de campos por rol y contexto |

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

### Siguiente bloque sugerido — Fase 19 Documentos UX real (orden recomendado)
1. **19.11** Separar documentos manuales vs generados por el sistema.
       - Definir que `ARCHIVO_CRONOLOGICO` no se crea desde el mismo modal generico.
       - Decidir que tipos admiten subida externa y cuales se generan desde datos del dominio.
2. **Migracion minima de estado**
       - Ampliar `EstadoDocumento` con `PENDIENTE_ADJUNTO`.
       - Mantener compatibilidad con los estados existentes (`BORRADOR`, `EMITIDO`, `CERRADO`, `VENCIDO`).
3. **DTO de alta real**
       - Crear DTO base o DTOs por tipo para evitar usar `Documento` JPA como payload bruto del formulario.
       - Validar en backend segun `TipoDocumento`.
4. **19.12** Wizard de alta en frontend (`documents.html`)
       - Paso 1: elegir tipo.
       - Paso 2: rellenar formulario especifico.
       - Paso 3: adjuntar PDF si aplica.
5. **19.13** Persistencia visible inmediata
       - Tras crear documento, insertar o refrescar fila en tabla con estado claro.
       - Si falla adjunto, dejar el registro en `PENDIENTE_ADJUNTO` con accion de reintento.
6. **19.14** Formularios especializados por tipo
       - Empezar por los tres de mayor valor: `DOCUMENTO_IDENTIFICACION`, `NOTIFICACION_PREVIA`, `CONTRATO`.
       - El resto puede entrar como segunda ola si mantienen una variante controlada y no un formulario universal.
7. **Refactor backend de generacion PDF**
       - Sacar del `DocumentoController` el `switch` provisional por tipo hacia una capa de servicio/document strategy.
       - Evitar que varios tipos heterogeneos compartan la misma plantilla PDF por comodidad.
8. **Cierre de la fase**
       - Crear documento debe ser una operacion comprensible y visible para usuario final.
       - La tabla `/documentos` debe reflejar el estado real sin depender de efectos colaterales del upload.

**Secuencia tecnica minima recomendada:**
- `EstadoDocumento` -> DTOs de alta -> servicio de creacion -> ajuste de `DocumentoController` -> rediseño `documents.html` -> validacion de tabla/preview/upload.

**Riesgo principal a vigilar:**
- No mezclar en el mismo flujo los documentos derivados del dominio (generables) con los documentos contractuales externos (subidos), porque esa mezcla es la que ha degradado la usabilidad actual.

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


---

### Siguiente Fase: UX y Corrección de Errores (En Cola)

**Objetivos:**
Resolver problemas de usabilidad y errores detectados en la interfaz tras la refactorización arquitectónica.

**Plan de Implementación:**

1. **Responsividad de Modales:**
   - **Problema:** Los modales no se adaptan correctamente al tamaño de la pantalla, volviéndose inaccesibles en ciertas resoluciones o dispositivos móviles.
   - **Solución:** Revisar el componente `crudModal` en `layouts/fragments.html` y las clases CSS asociadas (`modal-shell`). Se deben aplicar restricciones de altura máxima (`max-h-[90vh]`), desbordamiento interno (`overflow-y-auto` en el `modal-body`) y anchos responsivos (`w-full mx-4 sm:max-w-md`) para asegurar que el contenido siempre sea visible y desplazable sin salirse de la pantalla.

2. **Errores en la Gestión de Documentos (`documents.html`):**
        - **Problema:** El modulo de documentos no tiene usabilidad real. El alta usa el mismo formulario para todos los tipos documentales, aunque por dominio cada documento necesita campos distintos. Ademas, al crear un documento y subir el archivo adjunto, los iconos cambian repentinamente, la vista previa en el modal se rompe y el comportamiento general de la vista es inestable. En la practica, si no se completa bien la subida del PDF, el documento no aparece en lista ni se percibe como creado realmente.
   - **Solución:** 
               - Sustituir el formulario generico actual por formularios o secciones dinamicas por `TipoDocumento`.
               - Convertir la creacion en un flujo consistente: metadatos del documento -> subida de PDF -> confirmacion visual en tabla.
               - Investigar el script de carga de archivos y el re-renderizado de la tabla en `documents.html`.
               - Es muy probable que la inyección dinámica de HTML tras el guardado no esté respetando el marcado original o los eventos de `onclick` del visor PDF.
               - Garantizar que la llamada a `openIframePreviewModal` pase las URLs correctas del archivo recién creado.
               - Garantizar que un documento recien creado quede persistido y visible aunque el paso de adjunto falle, con un estado recuperable y accionable.

3. **Botón "Limpiar" en Informes (`reports.html`):**
   - **Problema:** El botón de limpiar los filtros no ejecuta ninguna acción.
   - **Solución:** Localizar el ID del botón en `reports.html` y asegurar que exista una función JS vinculada (ej. `limpiarFiltros()`) que limpie los campos `input` y re-ejecute la función de generación del reporte (`cargarDatos()` o similar) para devolverlo a su estado inicial.

**Estimación:** Medio día de trabajo.
