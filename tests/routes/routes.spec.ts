import { test, expect } from "@playwright/test";
import { RoutesPage } from "./routes-page";
import { LoginPage } from "../login/login-page";
import { ADMIN_CREDENTIALS } from "../helpers";

test.describe("Rutas", () => {
  test(
    "Pagina de rutas carga correctamente tras login",
    { tag: ["@critical", "@e2e", "@rutas", "@RUT-E2E-001"] },
    async ({ page }) => {
      const routesPage = new RoutesPage(page);
      await routesPage.loginAndGoto();
      await expect(page).toHaveURL(/\/routes/);
    }
  );

  test(
    "API GET /api/rutas devuelve JSON con sesion activa",
    { tag: ["@high", "@e2e", "@rutas", "@RUT-E2E-002"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/rutas");
      expect(response.status()).toBe(200);
      expect(response.headers()["content-type"]).toContain("application/json");
    }
  );

  test(
    "API GET /api/rutas devuelve array",
    { tag: ["@high", "@e2e", "@rutas", "@RUT-E2E-003"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/rutas");
      const body = await response.json();
      expect(Array.isArray(body)).toBe(true);
    }
  );

  test(
    "API GET /api/almacen devuelve JSON con sesion activa",
    { tag: ["@high", "@e2e", "@almacen", "@RUT-E2E-004"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/almacen");
      expect(response.status()).toBe(200);
      const body = await response.json();
      expect(Array.isArray(body)).toBe(true);
    }
  );

  test(
    "API GET /api/almacen/alertas-fifo devuelve array",
    { tag: ["@high", "@e2e", "@almacen", "@RUT-E2E-005"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/almacen/alertas-fifo");
      expect(response.status()).toBe(200);
      const body = await response.json();
      expect(Array.isArray(body)).toBe(true);
    }
  );

  test(
    "Rutas sin autenticacion redirige a login",
    { tag: ["@critical", "@e2e", "@rutas", "@RUT-E2E-006"] },
    async ({ page }) => {
      await page.goto("/routes");
      await expect(page).toHaveURL(/\/login/);
    }
  );
});
