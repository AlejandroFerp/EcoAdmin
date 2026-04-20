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
});
