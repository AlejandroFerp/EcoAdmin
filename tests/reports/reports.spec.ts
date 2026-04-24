import { test, expect } from "@playwright/test";
import { ReportsPage } from "./reports-page";
import { LoginPage } from "../login/login-page";
import { ADMIN_CREDENTIALS } from "../helpers";

test.describe("Informes", () => {
  test(
    "Pagina de informes carga correctamente tras login",
    { tag: ["@critical", "@e2e", "@informes", "@REP-E2E-001"] },
    async ({ page }) => {
      const reportsPage = new ReportsPage(page);
      await reportsPage.loginAndGoto();
      await expect(page).toHaveURL(/\/reports/);
      await expect(reportsPage.generateButton).toBeVisible();
      await expect(reportsPage.csvButton).toBeVisible();
    }
  );

  test(
    "Generar informe de traslados muestra tabla con resultados",
    { tag: ["@critical", "@e2e", "@informes", "@REP-E2E-002"] },
    async ({ page }) => {
      const reportsPage = new ReportsPage(page);
      await reportsPage.loginAndGoto();
      await reportsPage.selectReportTypeByText("Traslados");
      await reportsPage.generateReport();
      await expect(reportsPage.summaryText).not.toHaveText("Sin generar.");
    }
  );

  test(
    "Boton PDF solo visible para Final gestion",
    { tag: ["@high", "@e2e", "@informes", "@REP-E2E-003"] },
    async ({ page }) => {
      const reportsPage = new ReportsPage(page);
      await reportsPage.loginAndGoto();

      // Traslados - PDF oculto
      await reportsPage.selectReportTypeByText("Traslados");
      await expect(reportsPage.pdfButton).toBeHidden();

      // Final gestion - PDF visible
      await reportsPage.selectReportTypeByText("Final gestion");
      await expect(reportsPage.pdfButton).toBeVisible();
    }
  );

  test(
    "Generar checklist auditoria muestra semaforos",
    { tag: ["@high", "@e2e", "@informes", "@REP-E2E-004"] },
    async ({ page }) => {
      const reportsPage = new ReportsPage(page);
      await reportsPage.loginAndGoto();
      await reportsPage.selectReportTypeByText("Checklist auditoria");
      await reportsPage.generateReport();
      await expect(reportsPage.summaryText).toContainText("Checklist");
    }
  );

  test(
    "API /api/informes/traslados devuelve JSON con sesion activa",
    { tag: ["@high", "@e2e", "@informes", "@REP-E2E-005"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/informes/traslados");
      expect(response.status()).toBe(200);
      const body = await response.json();
      expect(body).toHaveProperty("columns");
      expect(body).toHaveProperty("rows");
      expect(body).toHaveProperty("total");
    }
  );

  test(
    "API /api/informes/final-gestion devuelve porLer",
    { tag: ["@high", "@e2e", "@informes", "@REP-E2E-006"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/informes/final-gestion");
      expect(response.status()).toBe(200);
      const body = await response.json();
      expect(body).toHaveProperty("porLer");
      expect(body).toHaveProperty("resumen");
    }
  );

  test(
    "API /api/informes/final-gestion/pdf devuelve application/pdf",
    { tag: ["@high", "@e2e", "@informes", "@REP-E2E-007"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/informes/final-gestion/pdf");
      expect(response.status()).toBe(200);
      expect(response.headers()["content-type"]).toContain("application/pdf");
    }
  );

  test(
    "API /api/informes/checklist-auditoria devuelve items y resumen",
    { tag: ["@high", "@e2e", "@informes", "@REP-E2E-008"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/informes/checklist-auditoria");
      expect(response.status()).toBe(200);
      const body = await response.json();
      expect(body).toHaveProperty("items");
      expect(body).toHaveProperty("resumen");
    }
  );

  test(
    "API /api/informes/inventario-almacen devuelve estructura",
    { tag: ["@high", "@e2e", "@informes", "@REP-E2E-009"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/informes/inventario-almacen");
      expect(response.status()).toBe(200);
      const body = await response.json();
      expect(body).toHaveProperty("columns");
      expect(body).toHaveProperty("rows");
      expect(body).toHaveProperty("resumen");
    }
  );

  test(
    "API /api/informes/trazabilidad/99999 devuelve 404",
    { tag: ["@medium", "@e2e", "@informes", "@REP-E2E-010"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/informes/trazabilidad/99999");
      expect(response.status()).toBe(404);
    }
  );

  test(
    "Informe sin autenticacion redirige a login",
    { tag: ["@critical", "@e2e", "@informes", "@REP-E2E-011"] },
    async ({ page }) => {
      await page.goto("/reports");
      await expect(page).toHaveURL(/\/login/);
    }
  );
});
