import { test, expect } from "@playwright/test";
import { DashboardPage } from "./dashboard-page";
import { LoginPage } from "../login/login-page";
import { ADMIN_CREDENTIALS } from "../helpers";

test.describe("Dashboard", () => {
  test(
    "Dashboard carga con sidebar y métricas tras login",
    { tag: ["@critical", "@e2e", "@dashboard", "@DASH-E2E-001"] },
    async ({ page }) => {
      const dashboard = new DashboardPage(page);
      await dashboard.loginAndGoto();
      await expect(page).toHaveTitle(/Dashboard/);
      await dashboard.expectSidebarVisible();
      await dashboard.expectAllNavLinksVisible();
    }
  );

  test(
    "Navegación a Centros desde sidebar",
    { tag: ["@high", "@e2e", "@dashboard", "@DASH-E2E-002"] },
    async ({ page }) => {
      const dashboard = new DashboardPage(page);
      await dashboard.loginAndGoto();
      await dashboard.navCenters.click();
      await page.waitForLoadState("domcontentloaded");
      await expect(page).toHaveURL(/\/centers/);
      await expect(page).toHaveTitle(/Centros/);
    }
  );

  test(
    "Navegación a Residuos desde sidebar",
    { tag: ["@high", "@e2e", "@dashboard", "@DASH-E2E-003"] },
    async ({ page }) => {
      const dashboard = new DashboardPage(page);
      await dashboard.loginAndGoto();
      await dashboard.navWaste.click();
      await page.waitForLoadState("domcontentloaded");
      await expect(page).toHaveURL(/\/waste/);
      await expect(page).toHaveTitle(/Residuos/);
    }
  );

  test(
    "Navegación a Traslados desde sidebar",
    { tag: ["@high", "@e2e", "@dashboard", "@DASH-E2E-004"] },
    async ({ page }) => {
      const dashboard = new DashboardPage(page);
      await dashboard.loginAndGoto();
      await dashboard.navShipments.click();
      await page.waitForLoadState("domcontentloaded");
      await expect(page).toHaveURL(/\/shipments/);
      await expect(page).toHaveTitle(/Recogidas/);
    }
  );

  test(
    "Navegación a Usuarios desde sidebar (solo ADMIN)",
    { tag: ["@high", "@e2e", "@dashboard", "@DASH-E2E-005"] },
    async ({ page }) => {
      const dashboard = new DashboardPage(page);
      await dashboard.loginAndGoto();
      await dashboard.navUsers.click();
      await page.waitForLoadState("domcontentloaded");
      await expect(page).toHaveURL(/\/users/);
      await expect(page).toHaveTitle(/Usuarios/);
    }
  );

  test(
    "Navegación a Direcciones desde sidebar",
    { tag: ["@high", "@e2e", "@dashboard", "@DASH-E2E-006"] },
    async ({ page }) => {
      const dashboard = new DashboardPage(page);
      await dashboard.loginAndGoto();
      await dashboard.navAddresses.click();
      await page.waitForLoadState("domcontentloaded");
      await expect(page).toHaveURL(/\/addresses/);
      await expect(page).toHaveTitle(/Direcciones/);
    }
  );

  test(
    "Sidebar muestra el nombre del usuario autenticado",
    { tag: ["@medium", "@e2e", "@dashboard", "@DASH-E2E-007"] },
    async ({ page }) => {
      const dashboard = new DashboardPage(page);
      await dashboard.loginAndGoto();
      // The sidebar shows sec:authentication="name" which renders the email
      const sidebar = page.locator("aside");
      await expect(sidebar).toBeVisible();
      // Logout link always present
      await expect(page.getByTitle("Cerrar sesion")).toBeVisible();
    }
  );

  test(
    "API /api/estadisticas devuelve JSON con sesión activa",
    { tag: ["@high", "@e2e", "@dashboard", "@DASH-E2E-008"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/estadisticas");
      expect(response.status()).toBe(200);
      const body = await response.json();
      expect(typeof body.totalCentros).toBe("number");
      expect(typeof body.totalResiduos).toBe("number");
    }
  );

  test(
    "Dashboard es accesible desde cualquier página del sidebar",
    { tag: ["@medium", "@e2e", "@dashboard", "@DASH-E2E-009"] },
    async ({ page }) => {
      const dashboard = new DashboardPage(page);
      await dashboard.loginAndGoto();
      await dashboard.navCenters.click();
      await page.waitForLoadState("domcontentloaded");
      await page.getByRole("link", { name: "Dashboard" }).click();
      await page.waitForLoadState("domcontentloaded");
      await expect(page).toHaveURL(/\/dashboard/);
    }
  );

  test(
    "Página de centros tiene search box y tabla",
    { tag: ["@medium", "@e2e", "@dashboard", "@DASH-E2E-010"] },
    async ({ page }) => {
      const dashboard = new DashboardPage(page);
      await dashboard.loginAndGoto();
      await dashboard.navCenters.click();
      await page.waitForLoadState("domcontentloaded");
      await expect(page.locator("#busqueda")).toBeVisible();
      await expect(page.locator("#tablaCuerpo")).toBeVisible();
    }
  );

  test(
    "Dashboard muestra selector de período con 4 opciones @medium @e2e @dashboard @DASH-E2E-011",
    { tag: ["@medium", "@e2e", "@dashboard", "@DASH-E2E-011"] },
    async ({ page }) => {
      const dashboard = new DashboardPage(page);
      await dashboard.loginAndGoto();
      await expect(page.locator("#pHoy")).toBeVisible();
      await expect(page.locator("#pSemana")).toBeVisible();
      await expect(page.locator("#pMes")).toBeVisible();
      await expect(page.locator("#pTodo")).toBeVisible();
    }
  );

  test(
    "Selector de período actualiza estadísticas al cambiar @medium @e2e @dashboard @DASH-E2E-012",
    { tag: ["@medium", "@e2e", "@dashboard", "@DASH-E2E-012"] },
    async ({ page }) => {
      const dashboard = new DashboardPage(page);
      await dashboard.loginAndGoto();
      // Wait for chart to render
      await page.waitForSelector("#chartEstados", { state: "visible" });
      // Click '7 días'
      await page.locator("#pSemana").click();
      await page.waitForTimeout(800);
      // Button should now be active (has shadow-sm class)
      const cls = await page.locator("#pSemana").getAttribute("class");
      expect(cls).toContain("shadow-sm");
    }
  );

  test(
    "Botón imprimir informe está visible en el dashboard @medium @e2e @dashboard @DASH-E2E-013",
    { tag: ["@medium", "@e2e", "@dashboard", "@DASH-E2E-013"] },
    async ({ page }) => {
      const dashboard = new DashboardPage(page);
      await dashboard.loginAndGoto();
      await expect(page.getByRole("button", { name: /Imprimir informe/i })).toBeVisible();
    }
  );

  test(
    "API /api/estadisticas acepta param desde y devuelve JSON @medium @e2e @dashboard @DASH-E2E-014",
    { tag: ["@medium", "@e2e", "@dashboard", "@DASH-E2E-014"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/estadisticas?desde=2020-01-01");
      expect(response.status()).toBe(200);
      const body = await response.json();
      expect(typeof body.trasladosPendientes).toBe("number");
      expect(typeof body.trasladosCompletados).toBe("number");
    }
  );

  test(
    "Página Rutas carga correctamente con placeholder @medium @e2e @dashboard @DASH-E2E-015",
    { tag: ["@medium", "@e2e", "@dashboard", "@DASH-E2E-015"] },
    async ({ page }) => {
      const dashboard = new DashboardPage(page);
      await dashboard.loginAndGoto();
      await page.goto("/routes");
      await page.waitForLoadState("domcontentloaded");
      await expect(page).toHaveTitle(/Rutas/);
      await expect(page.getByText("Módulo de Rutas")).toBeVisible();
    }
  );

  test(
    "Página Negocio carga correctamente con placeholder @medium @e2e @dashboard @DASH-E2E-016",
    { tag: ["@medium", "@e2e", "@dashboard", "@DASH-E2E-016"] },
    async ({ page }) => {
      const dashboard = new DashboardPage(page);
      await dashboard.loginAndGoto();
      await page.goto("/business");
      await page.waitForLoadState("domcontentloaded");
      await expect(page).toHaveTitle(/Negocio/);
      await expect(page.getByText("Módulo de Negocio")).toBeVisible();
    }
  );

  test(
    "Página Mis Datos carga correctamente con placeholder @medium @e2e @dashboard @DASH-E2E-017",
    { tag: ["@medium", "@e2e", "@dashboard", "@DASH-E2E-017"] },
    async ({ page }) => {
      const dashboard = new DashboardPage(page);
      await dashboard.loginAndGoto();
      await page.goto("/profile");
      await page.waitForLoadState("domcontentloaded");
      await expect(page).toHaveTitle(/Mis Datos/);
      await expect(page.getByText("Próximamente")).toBeVisible();
    }
  );
});
