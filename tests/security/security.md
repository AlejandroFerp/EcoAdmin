### E2E Tests: Seguridad

**Suite ID:** `SECU-E2E`
**Feature:** Control de acceso y protección de rutas API

---

## SECU-E2E-001 a 008 — Endpoints protegidos sin sesión

**Priority:** `critical`

**Preconditions:** Sin sesión activa (nueva pestaña/contexto limpio)

### Endpoints verificados:
- GET /api/centros
- GET /api/traslados
- GET /api/residuos
- GET /api/usuarios
- GET /api/estadisticas
- GET /api/direcciones
- POST /api/centros
- DELETE /api/centros/1

### Expected Result:
- Status 302 (redirección a login), 401 (Unauthorized) o 403 (Forbidden)

---

## SECU-E2E-009 — Expiración de sesión tras logout

**Priority:** `high`

### Flow Steps:
1. Login como admin → verificar GET /api/centros = 200
2. Logout → esperar redirección a /login
3. GET /api/centros → esperar 302/401/403

### Notes:
- `maxRedirects: 0` evita que Playwright siga la redirección automáticamente
