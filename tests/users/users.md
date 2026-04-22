### E2E Tests: Usuarios

**Suite ID:** `USUA-E2E`
**Feature:** Gestión de usuarios del sistema (solo ADMIN)

---

## USUA-E2E-001 a 004 — Verificación de UI

**Priority:** `critical/high/medium`

**Preconditions:** Autenticado como ADMIN

### Expected Result:
- Título: "EcoAdmin — Usuarios"
- Header: "Usuarios del sistema"
- Botón "Nuevo Usuario" visible
- Tabla con columnas Nombre, Email, Rol

---

## USUA-E2E-005 — API lista usuarios

**Priority:** `critical`

### Expected Result:
- GET /api/usuarios → array con al menos 1 elemento (admin)
- El array incluye usuario con email "admin@ecoadmin.com"

---

## USUA-E2E-006 a 007 — API crear y eliminar usuario

**Priority:** `critical/high`

### Notes:
- Usar timestamp en email para evitar conflictos de unicidad
- Limpiar siempre el usuario creado al finalizar el test
