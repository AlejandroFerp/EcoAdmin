import { test, expect } from "@playwright/test";
import { AddressesPage } from "./addresses-page";
import { LoginPage } from "../login/login-page";
import { ADMIN_CREDENTIALS } from "../helpers";

test.describe("Direcciones", () => {
  test(
    "Página de direcciones carga con título correcto",
    { tag: ["@critical", "@e2e", "@direcciones", "@DIRE-E2E-001"] },
    async ({ page }) => {
      const addressesPage = new AddressesPage(page);
      await addressesPage.loginAndGoto();
      await expect(page).toHaveURL(/\/addresses/);
      await expect(page).toHaveTitle("EcoAdmin — Direcciones");
    }
  );

  test(
    "Botón 'Añadir Direccion' visible en la página",
    { tag: ["@high", "@e2e", "@direcciones", "@DIRE-E2E-002"] },
    async ({ page }) => {
      const addressesPage = new AddressesPage(page);
      await addressesPage.loginAndGoto();
      await expect(addressesPage.addButton).toBeVisible();
    }
  );

  test(
    "Tabla de direcciones tiene columnas Calle, Ciudad, CP",
    { tag: ["@medium", "@e2e", "@direcciones", "@DIRE-E2E-003"] },
    async ({ page }) => {
      const addressesPage = new AddressesPage(page);
      await addressesPage.loginAndGoto();
      await expect(addressesPage.tablehead).toContainText("Calle");
      await expect(addressesPage.tablehead).toContainText("Ciudad");
      await expect(addressesPage.tablehead).toContainText("CP");
    }
  );

  test(
    "API GET /api/direcciones devuelve array JSON",
    { tag: ["@critical", "@e2e", "@direcciones", "@DIRE-E2E-004"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/direcciones");
      expect(response.status()).toBe(200);
      const body = await response.json();
      expect(Array.isArray(body)).toBe(true);
    }
  );

  test(
    "API POST /api/direcciones crea una dirección",
    { tag: ["@critical", "@e2e", "@direcciones", "@DIRE-E2E-005"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);

      const response = await page.request.post("/api/direcciones", {
        data: {
          calle: "Calle E2E 123",
          ciudad: "Alicante",
          codigoPostal: "03001",
          provincia: "Alicante",
          pais: "España",
        },
        headers: { "Content-Type": "application/json" },
      });
      expect([200, 201]).toContain(response.status());
      const body = await response.json();
      expect(body.id).toBeTruthy();

      // Cleanup
      await page.request.delete(`/api/direcciones/${body.id}`);
    }
  );

  test(
    "API DELETE /api/direcciones/{id} elimina la dirección creada",
    { tag: ["@high", "@e2e", "@direcciones", "@DIRE-E2E-006"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);

      const created = await page.request.post("/api/direcciones", {
        data: {
          calle: "Calle a Borrar",
          ciudad: "Valencia",
          codigoPostal: "46001",
          provincia: "Valencia",
          pais: "España",
        },
        headers: { "Content-Type": "application/json" },
      });
      const dir = await created.json();
      const deleteResponse = await page.request.delete(`/api/direcciones/${dir.id}`);
      expect([200, 204]).toContain(deleteResponse.status());
    }
  );
});
