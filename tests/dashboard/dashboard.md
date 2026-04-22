### E2E Tests: Dashboard y Navegación

**Suite ID:** `DASH-E2E`
**Feature:** Dashboard post-login con métricas y navegación lateral

---

## Test Case: `DASH-E2E-001` - Dashboard carga correctamente

**Priority:** `critical`

**Preconditions:**
- Autenticado como admin

### Flow Steps:
1. Iniciar sesión
2. Verificar carga del dashboard

### Expected Result:
- URL: `/dashboard`
- Título: "EcoAdmin — Dashboard"
- Sidebar visible con logo "EcoAdmin"
- Todos los enlaces de navegación visibles

---

## Test Case: `DASH-E2E-002` a `DASH-E2E-005` - Navegación desde sidebar

**Priority:** `high`

### Flow Steps:
1. Autenticarse
2. Hacer clic en enlace del sidebar (Centros / Residuos / Traslados / Usuarios)

### Expected Result (por enlace):
- Centros → `/centers`, título "EcoAdmin — Centros"
- Residuos → `/waste`, título "EcoAdmin — Residuos"
- Traslados → `/shipments`, título "EcoAdmin — Recogidas"
- Usuarios → `/users`, título "EcoAdmin — Usuarios"
