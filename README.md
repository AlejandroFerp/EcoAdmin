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
