### E2E Tests: Residuos

**Suite ID:** `RESI-E2E`
**Feature:** Gestión del catálogo de residuos peligrosos

---

## RESI-E2E-001 a 004 — Verificación de UI

**Priority:** `critical/high/medium`

### Expected Result:
- Título: "EcoAdmin — Residuos"
- Header: "Residuos peligrosos"
- Botón "Añadir Residuo" visible
- Tabla con columnas Codigo LER, Cantidad, Estado

---

## RESI-E2E-005 a 007 — API CRUD

**Priority:** `critical/high`

### Flow: GET→POST→DELETE

- GET /api/residuos → array JSON
- POST /api/residuos → {cantidad:150, unidad:"kg", estado:"ALMACENADO", codigoLER:"06 01 01*"} → 200/201 con id
- DELETE /api/residuos/{id} → 200/204

---

## RESI-E2E-008 — Búsqueda

**Priority:** `medium`

### Expected Result:
- #busqueda visible con placeholder que contiene "residuos"
