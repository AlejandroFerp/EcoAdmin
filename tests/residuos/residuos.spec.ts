import { test, expect } from "@playwright/test";
import { ResiduosPage } from "./residuos-page";
import { LoginPage } from "../login/login-page";
import { ADMIN_CREDENTIALS } from "../helpers";

test.describe("Residuos", () => {
  test(
    "Página de residuos carga con título correcto",
    { tag: ["@critical", "@e2e", "@residuos", "@RESI-E2E-001"] },
    async ({ page }) => {
      const residuosPage = new ResiduosPage(page);
      await residuosPage.loginAndGoto();
      await expect(page).toHaveURL(/\/public\/residuos/);
      await expect(page).toHaveTitle("EcoAdmin — Residuos");
    }
  );

  test(
    "Encabezado 'Residuos peligrosos' visible en header",
    { tag: ["@high", "@e2e", "@residuos", "@RESI-E2E-002"] },
    async ({ page }) => {
      const residuosPage = new ResiduosPage(page);
      await residuosPage.loginAndGoto();
      await expect(page.getByText("Residuos peligrosos")).toBeVisible();
    }
  );

  test(
    "Botón 'Añadir Residuo' visible en la página",
    { tag: ["@high", "@e2e", "@residuos", "@RESI-E2E-003"] },
    async ({ page }) => {
      const residuosPage = new ResiduosPage(page);
      await residuosPage.loginAndGoto();
      await expect(residuosPage.addButton).toBeVisible();
    }
  );

  test(
    "Tabla de residuos tiene columnas Codigo LER, Cantidad, Estado",
    { tag: ["@medium", "@e2e", "@residuos", "@RESI-E2E-004"] },
    async ({ page }) => {
      const residuosPage = new ResiduosPage(page);
      await residuosPage.loginAndGoto();
      await expect(residuosPage.tablehead).toContainText("Codigo LER");
      await expect(residuosPage.tablehead).toContainText("Cantidad");
      await expect(residuosPage.tablehead).toContainText("Estado");
    }
  );

  test(
    "API GET /api/residuos devuelve array JSON",
    { tag: ["@critical", "@e2e", "@residuos", "@RESI-E2E-005"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/residuos");
      expect(response.status()).toBe(200);
      const body = await response.json();
      expect(Array.isArray(body)).toBe(true);
    }
  );

  test(
    "API POST /api/residuos crea un residuo y devuelve el objeto",
    { tag: ["@critical", "@e2e", "@residuos", "@RESI-E2E-006"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);

      const residuoData = {
        cantidad: 150,
        unidad: "kg",
        estado: "ALMACENADO",
        codigoLER: "06 01 01*",
      };
      const response = await page.request.post("/api/residuos", {
        data: residuoData,
        headers: { "Content-Type": "application/json" },
      });
      expect([200, 201]).toContain(response.status());
      const body = await response.json();
      expect(body.id).toBeTruthy();
      expect(body.codigoLER).toBe("06 01 01*");

      // Cleanup
      await page.request.delete(`/api/residuos/${body.id}`);
    }
  );

  test(
    "API DELETE /api/residuos/{id} elimina el residuo creado",
    { tag: ["@high", "@e2e", "@residuos", "@RESI-E2E-007"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);

      const created = await page.request.post("/api/residuos", {
        data: { cantidad: 10, unidad: "t", estado: "ALMACENADO", codigoLER: "16 06 01*" },
        headers: { "Content-Type": "application/json" },
      });
      const residuo = await created.json();

      const deleteResponse = await page.request.delete(`/api/residuos/${residuo.id}`);
      expect([200, 204]).toContain(deleteResponse.status());
    }
  );

  test(
    "Caja de búsqueda de residuos presente",
    { tag: ["@medium", "@e2e", "@residuos", "@RESI-E2E-008"] },
    async ({ page }) => {
      const residuosPage = new ResiduosPage(page);
      await residuosPage.loginAndGoto();
      await expect(residuosPage.searchBox).toBeVisible();
      await expect(residuosPage.searchBox).toHaveAttribute("placeholder", /residuos/i);
    }
  );
});
