import { test, expect } from "@playwright/test";
import { LoginPage } from "../login/login-page";
import { ADMIN_CREDENTIALS } from "../helpers";

test.describe("Seguridad — Protección de rutas API", () => {
  test(
    "GET /api/centros sin sesión es rechazado",
    { tag: ["@critical", "@e2e", "@seguridad", "@SECU-E2E-001"] },
    async ({ page }) => {
      const response = await page.request.get("/api/centros", { maxRedirects: 0 });
      expect([302, 401, 403]).toContain(response.status());
    }
  );

  test(
    "GET /api/traslados sin sesión es rechazado",
    { tag: ["@critical", "@e2e", "@seguridad", "@SECU-E2E-002"] },
    async ({ page }) => {
      const response = await page.request.get("/api/traslados", { maxRedirects: 0 });
      expect([302, 401, 403]).toContain(response.status());
    }
  );

  test(
    "GET /api/residuos sin sesión es rechazado",
    { tag: ["@critical", "@e2e", "@seguridad", "@SECU-E2E-003"] },
    async ({ page }) => {
      const response = await page.request.get("/api/residuos", { maxRedirects: 0 });
      expect([302, 401, 403]).toContain(response.status());
    }
  );

  test(
    "GET /api/usuarios sin sesión es rechazado",
    { tag: ["@critical", "@e2e", "@seguridad", "@SECU-E2E-004"] },
    async ({ page }) => {
      const response = await page.request.get("/api/usuarios", { maxRedirects: 0 });
      expect([302, 401, 403]).toContain(response.status());
    }
  );

  test(
    "GET /api/estadisticas sin sesión es rechazado",
    { tag: ["@critical", "@e2e", "@seguridad", "@SECU-E2E-005"] },
    async ({ page }) => {
      const response = await page.request.get("/api/estadisticas", { maxRedirects: 0 });
      expect([302, 401, 403]).toContain(response.status());
    }
  );

  test(
    "POST /api/centros sin sesión es rechazado",
    { tag: ["@critical", "@e2e", "@seguridad", "@SECU-E2E-006"] },
    async ({ page }) => {
      const response = await page.request.post("/api/centros", {
        data: { nombre: "Intento no autenticado" },
        headers: { "Content-Type": "application/json" },
        maxRedirects: 0,
      });
      expect([302, 401, 403]).toContain(response.status());
    }
  );

  test(
    "DELETE /api/centros/1 sin sesión es rechazado",
    { tag: ["@critical", "@e2e", "@seguridad", "@SECU-E2E-007"] },
    async ({ page }) => {
      const response = await page.request.delete("/api/centros/1", { maxRedirects: 0 });
      expect([302, 401, 403]).toContain(response.status());
    }
  );

  test(
    "GET /api/direcciones sin sesión es rechazado",
    { tag: ["@critical", "@e2e", "@seguridad", "@SECU-E2E-008"] },
    async ({ page }) => {
      const response = await page.request.get("/api/direcciones", { maxRedirects: 0 });
      expect([302, 401, 403]).toContain(response.status());
    }
  );

  test(
    "Tras cerrar sesión, la API también rechaza con cookie expirada",
    { tag: ["@high", "@e2e", "@seguridad", "@SECU-E2E-009"] },
    async ({ page }) => {
      // Login
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);

      // Verify access
      const before = await page.request.get("/api/centros");
      expect(before.status()).toBe(200);

      // Logout via sidebar button (POST with CSRF)
      await page.locator('button[title="Cerrar sesion"]').click();
      await page.waitForURL(/\/login/);

      // After logout, session cookie is invalid
      const after = await page.request.get("/api/centros", { maxRedirects: 0 });
      expect([302, 401, 403]).toContain(after.status());
    }
  );
});
