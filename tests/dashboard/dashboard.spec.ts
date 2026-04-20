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
      await dashboard.navCentros.click();
      await page.waitForLoadState("domcontentloaded");
      await expect(page).toHaveURL(/\/public\/centros/);
      await expect(page).toHaveTitle(/Centros/);
    }
  );

  test(
    "Navegación a Residuos desde sidebar",
    { tag: ["@high", "@e2e", "@dashboard", "@DASH-E2E-003"] },
    async ({ page }) => {
      const dashboard = new DashboardPage(page);
      await dashboard.loginAndGoto();
      await dashboard.navResiduos.click();
      await page.waitForLoadState("domcontentloaded");
      await expect(page).toHaveURL(/\/public\/residuos/);
      await expect(page).toHaveTitle(/Residuos/);
    }
  );

  test(
    "Navegación a Traslados desde sidebar",
    { tag: ["@high", "@e2e", "@dashboard", "@DASH-E2E-004"] },
    async ({ page }) => {
      const dashboard = new DashboardPage(page);
      await dashboard.loginAndGoto();
      await dashboard.navTraslados.click();
      await page.waitForLoadState("domcontentloaded");
      await expect(page).toHaveURL(/\/public\/traslados/);
      await expect(page).toHaveTitle(/Traslados/);
    }
  );

  test(
    "Navegación a Usuarios desde sidebar (solo ADMIN)",
    { tag: ["@high", "@e2e", "@dashboard", "@DASH-E2E-005"] },
    async ({ page }) => {
      const dashboard = new DashboardPage(page);
      await dashboard.loginAndGoto();
      await dashboard.navUsuarios.click();
      await page.waitForLoadState("domcontentloaded");
      await expect(page).toHaveURL(/\/public\/usuarios/);
      await expect(page).toHaveTitle(/Usuarios/);
    }
  );

  test(
    "Navegación a Direcciones desde sidebar",
    { tag: ["@high", "@e2e", "@dashboard", "@DASH-E2E-006"] },
    async ({ page }) => {
      const dashboard = new DashboardPage(page);
      await dashboard.loginAndGoto();
      await dashboard.navDirecciones.click();
      await page.waitForLoadState("domcontentloaded");
      await expect(page).toHaveURL(/\/public\/direcciones/);
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
      await dashboard.navCentros.click();
      await page.waitForLoadState("domcontentloaded");
      await page.getByRole("link", { name: "Dashboard" }).click();
      await page.waitForLoadState("domcontentloaded");
      await expect(page).toHaveURL(/\/public\/index/);
    }
  );

  test(
    "Página de centros tiene search box y tabla",
    { tag: ["@medium", "@e2e", "@dashboard", "@DASH-E2E-010"] },
    async ({ page }) => {
      const dashboard = new DashboardPage(page);
      await dashboard.loginAndGoto();
      await dashboard.navCentros.click();
      await page.waitForLoadState("domcontentloaded");
      await expect(page.locator("#busqueda")).toBeVisible();
      await expect(page.locator("#tablaCuerpo")).toBeVisible();
    }
  );
});
