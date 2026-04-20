### E2E Tests: Traslados y API

**Suite ID:** `TRAS-E2E`
**Feature:** Página de traslados y verificación de API REST

---

## Test Case: `TRAS-E2E-001` - Página traslados carga

**Priority:** `critical`

**Preconditions:**
- Autenticado como admin

### Flow Steps:
1. Autenticar
2. Navegar a `/public/traslados`

### Expected Result:
- URL: `/public/traslados`
- Título: "EcoAdmin — Traslados"

---

## Test Case: `TRAS-E2E-002` a `TRAS-E2E-004` - API devuelve JSON

**Priority:** `high`

**Preconditions:**
- Sesión HTTP activa (login previo en el mismo contexto de página)

### Flow Steps:
1. Hacer clic en "Acceder" con credenciales admin
2. Realizar GET a `/api/traslados` (o `/api/centros`, `/api/residuos`)

### Expected Result:
- Status 200
- Content-Type: application/json

---

## Test Case: `TRAS-E2E-005` - API rechaza sin autenticar

**Priority:** `high`

### Flow Steps:
1. GET `/api/traslados` sin sesión (sin seguir redirecciones)

### Expected Result:
- Status 302 / 401 / 403
