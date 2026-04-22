### E2E Tests: Estadísticas

**Suite ID:** `ESTA-E2E`
**Feature:** API de métricas globales del sistema

---

## ESTA-E2E-001 a 003 — Validación de respuesta API

**Priority:** `critical/high`

**Preconditions:** Autenticado como admin

### Fields esperados en respuesta:
- `totalCentros` (number ≥ 0)
- `totalResiduos` (number ≥ 0)
- `trasladosPendientes` (number ≥ 0)
- `trasladosEnTransito` (number ≥ 0)
- `trasladosEntregados` (number ≥ 0)
- `trasladosCompletados` (number ≥ 0)
- `residuosPorCentro` (object/map)

---

## ESTA-E2E-004 — Rechazo sin autenticar

**Priority:** `high`

### Expected: status 302/401/403

---

## ESTA-E2E-005 — Consistencia de datos

**Priority:** `high`

### Expected:
- `totalCentros` en stats == `length` del array de /api/centros
