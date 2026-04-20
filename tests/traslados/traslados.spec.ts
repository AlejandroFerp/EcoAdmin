import { test, expect } from "@playwright/test";
import { TrasladosPage } from "./traslados-page";
import { LoginPage } from "../login/login-page";
import { ADMIN_CREDENTIALS } from "../helpers";

test.describe("Traslados", () => {
  test(
    "Página de traslados carga correctamente tras login",
    { tag: ["@critical", "@e2e", "@traslados", "@TRAS-E2E-001"] },
    async ({ page }) => {
      const trasladosPage = new TrasladosPage(page);
      await trasladosPage.loginAndGoto();
      await expect(page).toHaveURL(/\/public\/traslados/);
      await expect(page).toHaveTitle(/Traslados/);
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
});
