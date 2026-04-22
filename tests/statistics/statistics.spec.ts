import { test, expect } from "@playwright/test";
import { LoginPage } from "../login/login-page";
import { ADMIN_CREDENTIALS } from "../helpers";

test.describe("Estadísticas", () => {
  test(
    "API /api/estadisticas devuelve 200 con JSON",
    { tag: ["@critical", "@e2e", "@estadisticas", "@ESTA-E2E-001"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/estadisticas");
      expect(response.status()).toBe(200);
      expect(response.headers()["content-type"]).toContain("application/json");
    }
  );

  test(
    "Respuesta contiene todos los campos del DTO de estadísticas",
    { tag: ["@critical", "@e2e", "@estadisticas", "@ESTA-E2E-002"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/estadisticas");
      const body = await response.json();
      expect(body).toHaveProperty("totalCentros");
      expect(body).toHaveProperty("totalResiduos");
      expect(body).toHaveProperty("trasladosPendientes");
      expect(body).toHaveProperty("trasladosEnTransito");
      expect(body).toHaveProperty("trasladosEntregados");
      expect(body).toHaveProperty("trasladosCompletados");
      expect(body).toHaveProperty("residuosPorCentro");
    }
  );

  test(
    "Los valores numéricos de estadísticas son ≥ 0",
    { tag: ["@high", "@e2e", "@estadisticas", "@ESTA-E2E-003"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/estadisticas");
      const body = await response.json();
      expect(body.totalCentros).toBeGreaterThanOrEqual(0);
      expect(body.totalResiduos).toBeGreaterThanOrEqual(0);
      expect(body.trasladosPendientes).toBeGreaterThanOrEqual(0);
      expect(body.trasladosCompletados).toBeGreaterThanOrEqual(0);
    }
  );

  test(
    "API /api/estadisticas rechaza petición no autenticada",
    { tag: ["@high", "@e2e", "@estadisticas", "@ESTA-E2E-004"] },
    async ({ page }) => {
      const response = await page.request.get("/api/estadisticas", { maxRedirects: 0 });
      expect([302, 401, 403]).toContain(response.status());
    }
  );

  test(
    "totalCentros en estadísticas coincide con conteo en GET /api/centros",
    { tag: ["@high", "@e2e", "@estadisticas", "@ESTA-E2E-005"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const [statsResp, centrosResp] = await Promise.all([
        page.request.get("/api/estadisticas"),
        page.request.get("/api/centros"),
      ]);
      const stats = await statsResp.json();
      const centros = await centrosResp.json();
      expect(stats.totalCentros).toBe(centros.length);
    }
  );
});
