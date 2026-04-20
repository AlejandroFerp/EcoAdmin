### E2E Tests: Autenticación

**Suite ID:** `LOGIN-E2E`
**Feature:** Login y control de acceso por Spring Security form login

---

## Test Case: `LOGIN-E2E-001` - Redirección de raíz a login sin sesión

**Priority:** `critical`

**Tags:** @e2e @auth @critical

**Description/Objective:** Verificar que cualquier acceso sin autenticar redirige al formulario de login.

**Preconditions:**
- Aplicación arrancada en `http://localhost:8080`
- Sin sesión activa

### Flow Steps:
1. Navegar a `/`

### Expected Result:
- URL final contiene `/public/login`
- Título de página contiene "EcoAdmin"

---

## Test Case: `LOGIN-E2E-002` - Login exitoso con admin

**Priority:** `critical`

**Preconditions:**
- Credenciales: `admin@ecoadmin.com` / `admin123`

### Flow Steps:
1. Ir a `/public/login`
2. Rellenar Email con `admin@ecoadmin.com`
3. Rellenar Contraseña con `admin123`
4. Hacer clic en "Acceder"

### Expected Result:
- Redirige a `/public/index`
- Título contiene "Dashboard"

---

## Test Case: `LOGIN-E2E-003` - Error con credenciales incorrectas

**Priority:** `high`

### Flow Steps:
1. Ir a `/public/login`
2. Rellenar credenciales inválidas
3. Enviar formulario

### Expected Result:
- URL contiene `?error`
- Mensaje "Email o contraseña incorrectos." visible

---

## Test Case: `LOGIN-E2E-004` - Protección de ruta autenticada

**Priority:** `high`

### Flow Steps:
1. Acceder directamente a `/public/index` sin sesión

### Expected Result:
- Redirige a `/public/login`

---

## Test Case: `LOGIN-E2E-005` - Cierre de sesión

**Priority:** `high`

### Flow Steps:
1. Iniciar sesión como admin
2. Hacer clic en el enlace de cierre de sesión (title="Cerrar sesion")

### Expected Result:
- Redirige a `/public/login?logout`
- Mensaje "Sesión cerrada correctamente." visible
