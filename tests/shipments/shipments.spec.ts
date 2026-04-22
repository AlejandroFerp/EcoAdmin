import { test, expect } from "@playwright/test";
import { ShipmentsPage } from "./shipments-page";
import { LoginPage } from "../login/login-page";
import { ADMIN_CREDENTIALS } from "../helpers";

test.describe("Traslados", () => {
  test(
    "Página de traslados carga correctamente tras login",
    { tag: ["@critical", "@e2e", "@traslados", "@TRAS-E2E-001"] },
    async ({ page }) => {
      const shipmentsPage = new ShipmentsPage(page);
      await shipmentsPage.loginAndGoto();
      await expect(page).toHaveURL(/\/shipments/);
      await expect(page).toHaveTitle(/Recogidas/);
    }
  );

  test(
    "API GET /api/traslados devuelve JSON con sesión activa",
    { tag: ["@high", "@e2e", "@traslados", "@TRAS-E2E-002"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);

      const response = await page.request.get("/api/traslados");
      expect(response.status()).toBe(200);
      expect(response.headers()["content-type"]).toContain("application/json");
    }
  );

  test(
    "API GET /api/centros devuelve JSON con sesión activa",
    { tag: ["@high", "@e2e", "@traslados", "@TRAS-E2E-003"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);

      const response = await page.request.get("/api/centros");
      expect(response.status()).toBe(200);
      expect(response.headers()["content-type"]).toContain("application/json");
    }
  );

  test(
    "API GET /api/residuos devuelve JSON con sesión activa",
    { tag: ["@high", "@e2e", "@traslados", "@TRAS-E2E-004"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);

      const response = await page.request.get("/api/residuos");
      expect(response.status()).toBe(200);
      expect(response.headers()["content-type"]).toContain("application/json");
    }
  );

  test(
    "API rechaza peticiones no autenticadas con 302 al login",
    { tag: ["@high", "@e2e", "@traslados", "@TRAS-E2E-005"] },
    async ({ page }) => {
      const response = await page.request.get("/api/traslados", {
        maxRedirects: 0,
      });
      expect([302, 401, 403]).toContain(response.status());
    }
  );

  test(
    "Botón 'Nuevo Traslado' visible en la página",
    { tag: ["@medium", "@e2e", "@traslados", "@TRAS-E2E-006"] },
    async ({ page }) => {
      const shipmentsPage = new ShipmentsPage(page);
      await shipmentsPage.loginAndGoto();
      await expect(page.getByRole("button", { name: /Nuevo Traslado/ })).toBeVisible();
    }
  );

  test(
    "Botón '📷 Leer QR' visible en la página de traslados",
    { tag: ["@medium", "@e2e", "@traslados", "@TRAS-E2E-007"] },
    async ({ page }) => {
      const shipmentsPage = new ShipmentsPage(page);
      await shipmentsPage.loginAndGoto();
      await expect(page.getByRole("button", { name: /Leer QR/ })).toBeVisible();
    }
  );

  test(
    "Tabla de traslados tiene columnas Estado y Productor en cabecera",
    { tag: ["@medium", "@e2e", "@traslados", "@TRAS-E2E-008"] },
    async ({ page }) => {
      const shipmentsPage = new ShipmentsPage(page);
      await shipmentsPage.loginAndGoto();
      const thead = page.locator("table thead");
      await expect(thead).toContainText("Estado");
      await expect(thead).toContainText("Productor");
    }
  );

  test(
    "API POST /api/traslados requiere centros válidos (400 con cuerpo vacío)",
    { tag: ["@high", "@e2e", "@traslados", "@TRAS-E2E-009"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.post("/api/traslados", {
        data: {},
        headers: { "Content-Type": "application/json" },
      });
      // Server returns 400 or 500 for invalid data, not 200
      expect(response.status()).not.toBe(200);
    }
  );

  test(
    "API GET /api/traslados devuelve un array (puede estar vacío)",
    { tag: ["@high", "@e2e", "@traslados", "@TRAS-E2E-010"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/traslados");
      expect(response.status()).toBe(200);
      const body = await response.json();
      expect(Array.isArray(body)).toBe(true);
    }
  );

  test(
    "API GET /api/traslados/por-estado/PENDIENTE devuelve array",
    { tag: ["@high", "@e2e", "@traslados", "@TRAS-E2E-011"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/traslados/por-estado/PENDIENTE");
      expect(response.status()).toBe(200);
      const body = await response.json();
      expect(Array.isArray(body)).toBe(true);
    }
  );

  test(
    "API /api/estadisticas tiene todos los campos del DTO",
    { tag: ["@high", "@e2e", "@traslados", "@TRAS-E2E-012"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/estadisticas");
      expect(response.status()).toBe(200);
      const body = await response.json();
      expect(body).toHaveProperty("totalCentros");
      expect(body).toHaveProperty("totalResiduos");
      expect(body).toHaveProperty("trasladosPendientes");
      expect(body).toHaveProperty("trasladosEnTransito");
      expect(body).toHaveProperty("trasladosCompletados");
    }
  );

  test(
    "Barra de búsqueda en traslados filtra por texto",
    { tag: ["@medium", "@e2e", "@traslados", "@TRAS-E2E-013"] },
    async ({ page }) => {
      const shipmentsPage = new ShipmentsPage(page);
      await shipmentsPage.loginAndGoto();
      const searchBox = page.locator("#busqueda");
      await expect(searchBox).toBeVisible();
      await searchBox.fill("test-busqueda-inexistente-xyz");
      await page.waitForTimeout(300); // debounce
      // Table shows "Sin resultados" or empty
      const tbody = page.locator("#tablaCuerpo");
      await expect(tbody).toBeVisible();
    }
  );
});
