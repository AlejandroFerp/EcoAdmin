# EcoAdmin — Gestión de traslados de residuos peligrosos

Proyecto intermodular de 2.º DAM. Sistema de trazabilidad para el traslado de residuos peligrosos (baterías de litio) entre centros productores y centros gestores, con generación de documentación legal y notificaciones automatizadas.

## Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Backend | Spring Boot 4.0.0 + Java 25 |
| Persistencia | SQLite 3 + Hibernate 7 + Spring Data JPA |
| Seguridad | Spring Security 6 + BCrypt |
| Plantillas | Thymeleaf + Tailwind CSS (CDN) + DaisyUI 4 |
| PDF | OpenPDF 2.0.3 |
| QR | ZXing 3.5.3 |
| Email | Spring Mail (SMTP, async) |
| API docs | springdoc-openapi 2.8.6 (OpenAPI 3.1) |
| Gráficos | Chart.js 4.4.3 (CDN) |

## Requisitos

- Java 25 LTS (Temurin 25, instalado en `~/.jdk/jdk-25`)
- Maven Wrapper incluido (`mvnw` / `mvnw.cmd`)
- No requiere base de datos externa — SQLite embebida (`ecoadmin.db`)

## Arranque rápido

```bash
# Windows
$env:JAVA_HOME = "$env:USERPROFILE\.jdk\jdk-25"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\mvnw.cmd spring-boot:run
```

La aplicación arranca en `http://localhost:8080` y redirige automáticamente al login.

**Credenciales por defecto** (creadas al iniciar si la base de datos está vacía):

| Campo | Valor |
|-------|-------|
| Email | `admin@ecoadmin.com` |
| Contraseña | `admin123` |
| Rol | `ADMIN` |

## Configuración de email (opcional)

El email es asíncrono y falla en silencio si no se configura. Para activarlo:

```bash
# Variables de entorno (o en application.properties)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu@gmail.com
MAIL_PASSWORD=app-password
```

## Estructura del proyecto

```
src/main/java/com/iesdoctorbalmis/spring/
├── Application.java                  # @SpringBootApplication + @EnableAsync
├── config/
│   ├── DataInitializer.java          # Crea usuario admin si la DB está vacía
│   ├── SeguridadConfig.java          # Spring Security 6 + CORS centralizado
│   └── SwaggerConfig.java            # Metadatos OpenAPI
├── controladores/
│   ├── CentroController.java         # CRUD /api/centros
│   ├── DireccionController.java      # CRUD /api/direcciones
│   ├── EstadisticasController.java   # GET /api/estadisticas
│   ├── ResiduoController.java        # CRUD /api/residuos
│   ├── TrasladoController.java       # CRUD + estado + historial + PDF + QR
│   ├── UsuarioController.java        # CRUD /api/usuarios
│   └── ZonaPublicaController.java    # /public/index (dashboard Thymeleaf)
├── dto/
│   └── EstadisticasDTO.java          # Record con métricas del sistema
├── modelo/
│   ├── enums/
│   │   ├── EstadoTraslado.java       # PENDIENTE | EN_TRANSITO | ENTREGADO | COMPLETADO
│   │   └── Rol.java                  # PRODUCTOR | GESTOR | TRANSPORTISTA | ADMIN
│   ├── Centro.java
│   ├── Direccion.java
│   ├── EventoTraslado.java           # Línea de auditoría por cada cambio de estado
│   ├── Residuo.java                  # Incluye código LER
│   ├── Traslado.java                 # Entidad principal del dominio
│   └── Usuario.java
├── repository/                       # Interfaces JPA (Spring Data)
└── servicios/
    ├── EmailService.java             # Notificaciones @Async
    ├── PdfService.java               # Carta de porte, notificación, certificado
    ├── QrService.java                # Genera PNG 300×300 con ZXing
    └── *Service / *ServiceDB         # Interfaz + implementación para cada entidad
```

## API REST

Documentación interactiva disponible en `http://localhost:8080/swagger-ui/index.html` una vez arrancada la aplicación.

### Endpoints principales

#### Traslados — `/api/traslados`

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/traslados` | Lista todos los traslados |
| `POST` | `/api/traslados` | Crea un traslado nuevo |
| `PUT` | `/api/traslados/{id}` | Actualiza datos de un traslado |
| `DELETE` | `/api/traslados/{id}` | Elimina un traslado |
| `GET` | `/api/traslados/por-estado/{estado}` | Filtra por estado |
| `PATCH` | `/api/traslados/{id}/estado` | Cambia estado y registra evento |
| `GET` | `/api/traslados/{id}/historial` | Historial de eventos del traslado |
| `GET` | `/api/traslados/{id}/qr` | Genera QR PNG del traslado |
| `GET` | `/api/traslados/{id}/pdf/carta-porte` | Descarga carta de porte PDF |
| `GET` | `/api/traslados/{id}/pdf/notificacion` | Descarga notificación de traslado PDF |
| `GET` | `/api/traslados/{id}/pdf/certificado` | Descarga certificado de recepción PDF |

#### Otros recursos

| Prefijo | Recurso |
|---------|---------|
| `/api/centros` | Centros productores y gestores |
| `/api/residuos` | Residuos con código LER |
| `/api/usuarios` | Usuarios del sistema |
| `/api/direcciones` | Direcciones asociadas a centros |
| `/api/estadisticas` | Métricas globales del sistema |

### Modelo de estados de un traslado

```
PENDIENTE → EN_TRANSITO → ENTREGADO → COMPLETADO
```

Cada transición genera automáticamente un `EventoTraslado` con la marca de tiempo, el usuario que ejecutó el cambio y un comentario opcional.

## Seguridad

- Autenticación por formulario (Spring Security). Sesión HTTP estándar.
- Contraseñas almacenadas con BCrypt.
- Rutas públicas: `/public/**`, `/webjars/**`, `/css/**`, `/swagger-ui/**`, `/v3/api-docs/**`.
- CORS configurado centralmente para `localhost:8080` y `localhost:3000`.
- Roles: `PRODUCTOR`, `GESTOR`, `TRANSPORTISTA`, `ADMIN`.

## Tests E2E (Playwright)

Tests end-to-end con [Playwright](https://playwright.dev) que verifican el flujo real en navegador contra `http://localhost:8080`.

### Requisitos

- Node.js 18+
- La aplicación Spring Boot **debe estar arrancada** antes de ejecutar los tests

### Instalación

```bash
cd tests
npm install
npx playwright install chromium
```

### Ejecución

```bash
# Todos los tests (headless)
npm test

# Con interfaz visual interactiva
npm run test:ui

# Con navegador visible
npm run test:headed

# En modo debug (paso a paso)
npm run test:debug

# Abrir informe HTML del último run
npm run test:report
```

### Filtrar por suite, tag o ID

```bash
npx playwright test --grep "Login"
npx playwright test --grep "@critical"
npx playwright test --grep "@CENT-E2E"
npx playwright test tests/login/
npx playwright test tests/centros/
npx playwright test tests/seguridad/
```

### Cobertura de tests

| Suite | ID | Tests | Descripción |
|-------|----|-------|-------------|
| Login | `LOGIN-E2E` | 10 | Redirección sin sesión, login correcto, error credenciales, protección de ruta, cierre de sesión, título de página, campo enmascarado, labels visibles, HTML5 required, placeholder |
| Dashboard | `DASH-E2E` | 10 | Carga post-login, navegación a Centros / Residuos / Traslados / Usuarios / Direcciones, sidebar, API `/api/estadisticas`, breadcrumb |
| Traslados | `TRAS-E2E` | 13 | Carga página, GET API, botones QR/Nuevo, columnas, POST validación, por-estado, historial, búsqueda |
| Centros | `CENT-E2E` | 9 | UI (título, heading, botón, búsqueda, columnas) + CRUD completo API (GET / POST / PUT / DELETE) |
| Residuos | `RESI-E2E` | 8 | UI (título, heading, botón, columnas, búsqueda) + CRUD API (GET / POST / DELETE) |
| Usuarios | `USUA-E2E` | 7 | UI (título, heading, botón, columnas) + API GET lista + POST / DELETE usuarios |
| Estadísticas | `ESTA-E2E` | 5 | Campos DTO, valores ≥ 0, rechazo sin sesión, consistencia con `/api/centros` |
| Seguridad | `SECU-E2E` | 9 | Protección de todos los endpoints sin sesión (GET + POST + DELETE) + expiración tras logout |
| Direcciones | `DIRE-E2E` | 6 | UI (título, botón, columnas) + CRUD API (GET / POST / DELETE) |

**77 tests** en total — todos en Chromium headless.

### Estructura de tests

```
tests/
├── package.json                  # Dependencias Playwright
├── playwright.config.ts          # Config: baseURL, retries, screenshot on fail
├── base-page.ts                  # Clase base para todos los Page Objects
├── helpers.ts                    # Credenciales y rutas compartidas
├── login/
│   ├── login-page.ts             # Page Object: formulario de login
│   ├── login.spec.ts             # Tests LOGIN-E2E-001..010
│   └── login.md                  # Documentación de casos
├── dashboard/
│   ├── dashboard-page.ts         # Page Object: sidebar y navegación
│   ├── dashboard.spec.ts         # Tests DASH-E2E-001..010
│   └── dashboard.md              # Documentación de casos
├── traslados/
│   ├── traslados-page.ts         # Page Object: página de traslados
│   ├── traslados.spec.ts         # Tests TRAS-E2E-001..013
│   └── traslados.md              # Documentación de casos
├── centros/
│   ├── centros-page.ts           # Page Object: gestión de centros
│   ├── centros.spec.ts           # Tests CENT-E2E-001..009
│   └── centros.md                # Documentación de casos
├── residuos/
│   ├── residuos-page.ts          # Page Object: catálogo de residuos
│   ├── residuos.spec.ts          # Tests RESI-E2E-001..008
│   └── residuos.md               # Documentación de casos
├── usuarios/
│   ├── usuarios-page.ts          # Page Object: gestión de usuarios
│   ├── usuarios.spec.ts          # Tests USUA-E2E-001..007
│   └── usuarios.md               # Documentación de casos
├── direcciones/
│   ├── direcciones-page.ts       # Page Object: gestión de direcciones
│   ├── direcciones.spec.ts       # Tests DIRE-E2E-001..006
│   └── direcciones.md            # Documentación de casos
├── estadisticas/
│   ├── estadisticas.spec.ts      # Tests ESTA-E2E-001..005
│   └── estadisticas.md           # Documentación de casos
└── seguridad/
    ├── seguridad.spec.ts         # Tests SECU-E2E-001..009
    └── seguridad.md              # Documentación de casos
```

---

## Estado del MVP

| Funcionalidad | Estado |
|---------------|--------|
| Autenticación y roles | ✅ Completo |
| Modelo de dominio (Traslado, Centro, Residuo, Direccion, Usuario) | ✅ Completo |
| API REST completa con Spring Data JPA | ✅ Completo |
| Documentación Swagger (OpenAPI 3.1) | ✅ Completo |
| Generación de PDF (carta de porte, notificación, certificado) | ✅ Completo |
| Generación de QR por traslado | ✅ Completo |
| Notificaciones email asíncronas | ✅ Completo (requiere config SMTP) |
| Dashboard con métricas y gráficos (Chart.js) | ✅ Completo |
| Historial de auditoría por traslado | ✅ Completo |
| Interfaz de gestión CRUD (frontend) | ⬜ Pendiente |
| Tests E2E Playwright (9 suites, todas las páginas + API) | ✅ 77 tests — `tests/` |

## Antecedente: prototipo Reflex

Repositorio: [AlejandroFerp/app](https://github.com/AlejandroFerp/app)

Antes del backend Spring Boot se construyó un prototipo con [Reflex](https://reflex.dev) 0.8.12 (Python) + SQLite + SQLModel + Alembic para validar el modelo de dominio y explorar la UX de gestión de entidades.

### Stack del prototipo

| Elemento | Tecnología |
|----------|-----------|
| Framework UI | Reflex 0.8.12 + Reflex Enterprise |
| Base de datos | SQLite (`reflex.db`) vía SQLModel |
| Migraciones | Alembic |
| Tablas UI | AG Grid (reflex-enterprise) |
| Catálogo LER | `lista_ler.json` (119 KB, ~7.000 códigos) |

### Modelo del prototipo

```
Address          (street, city, postal_code, country)
ListaLER         (code, description)
Waste            (name, ler_id → ListaLER)
Client           (name, cif, email, phone, address_id, wastes M2M)
Center           (business_name, nima, rate, client_id, address_id)
Usuario          (nombre, edad, email, activo)
```

### Pantallas implementadas

| Ruta | Contenido |
|------|-----------|
| `/addresses` | CRUD de direcciones |
| `/centers` | CRUD de centros (con cliente y dirección relacionados) |
| `/clients` | CRUD de clientes (con selección múltiple de residuos) |
| `/listaler` | Catálogo de códigos LER |
| `/usuarios` | Gestión de usuarios |
| `/wastes` | CRUD de residuos con código LER |

### Patrón de estado

El prototipo usa un `FormState` genérico que genera formularios por reflexión a partir de los `__fields__` del modelo, evitando duplicar lógica CRUD por entidad. Cada entidad tiene además su propio `XxxState` para gestión específica (carga, edición, borrado con confirmación mediante `BaseState`).

### Evolución hacia Spring Boot

| Prototipo (Reflex) | MVP (Spring Boot) | Cambio |
|--------------------|-------------------|--------|
| `Client` (nombre empresa, CIF, email) | `Usuario` con enum `Rol` | Fusión + roles |
| `ListaLER` como entidad separada | Campo `codigoLER` en `Residuo` | Simplificación |
| `Center.rate` (tarifa) | Sin equivalente aún | Pendiente |
| Relación M2M Client↔Waste | Sin equivalente directo | Pendiente |
| Sin entidad de traslado | `Traslado` + `EventoTraslado` | **Núcleo del dominio** |
| Sin documentos legales | PDF carta de porte, notificación, certificado | Nuevo |
| Sin trazabilidad de estados | Historial de auditoría por traslado | Nuevo |

## Repositorio

`https://github.com/AlejandroFerp/EcoAdmin`
