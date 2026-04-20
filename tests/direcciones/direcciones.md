### E2E Tests: Direcciones

**Suite ID:** `DIRE-E2E`
**Feature:** Gestión de direcciones postales

---

## DIRE-E2E-001 a 003 — Verificación de UI

**Priority:** `critical/high/medium`

### Expected Result:
- Título: "EcoAdmin — Direcciones"
- Botón "Añadir Direccion" visible
- Tabla con columnas Calle, Ciudad, CP

---

## DIRE-E2E-004 a 006 — API CRUD

**Priority:** `critical/high`

### Flow:
- GET /api/direcciones → array JSON
- POST /api/direcciones → {calle, ciudad, codigoPostal, provincia, pais} → 200/201 con id
- DELETE /api/direcciones/{id} → 200/204
