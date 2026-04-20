### E2E Tests: Centros

**Suite ID:** `CENT-E2E`
**Feature:** Gestión de centros productores y gestores

---

## CENT-E2E-001 a 005 — Verificación de UI

**Priority:** `critical/high/medium`

**Preconditions:** Autenticado como admin

### Flow Steps:
1. Login con admin@ecoadmin.com
2. Navegar a `/public/centros`

### Expected Result:
- Título: "EcoAdmin — Centros"
- Header: "Centros de tratamiento"
- Botón "Añadir Centro" visible
- Tabla con columnas Nombre, Tipo, Ciudad

---

## CENT-E2E-006 — API GET /api/centros

**Priority:** `critical`

### Expected Result:
- Status 200, body es un array JSON

---

## CENT-E2E-007 — API POST /api/centros (crear)

**Priority:** `critical`

### Expected Result:
- Status 200 o 201, body contiene `id` y `nombre` del centro creado
- Limpieza: DELETE del centro creado

---

## CENT-E2E-008 — API PUT /api/centros/{id} (actualizar)

**Priority:** `high`

### Expected Result:
- Status 200 o 204

---

## CENT-E2E-009 — API DELETE /api/centros/{id}

**Priority:** `high`

### Expected Result:
- Status 200 o 204
