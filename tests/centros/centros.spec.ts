import { test, expect } from "@playwright/test";
import { CentrosPage } from "./centros-page";
import { LoginPage } from "../login/login-page";
import { ADMIN_CREDENTIALS } from "../helpers";

test.describe("Centros", () => {
  test(
    "Página de centros carga con título correcto",
    { tag: ["@critical", "@e2e", "@centros", "@CENT-E2E-001"] },
    async ({ page }) => {
      const centrosPage = new CentrosPage(page);
      await centrosPage.loginAndGoto();
      await expect(page).toHaveURL(/\/public\/centros/);
      await expect(page).toHaveTitle("EcoAdmin — Centros");
    }
  );

  test(
    "Encabezado 'Centros de tratamiento' visible",
    { tag: ["@high", "@e2e", "@centros", "@CENT-E2E-002"] },
    async ({ page }) => {
      const centrosPage = new CentrosPage(page);
      await centrosPage.loginAndGoto();
      await expect(page.getByText("Centros de tratamiento")).toBeVisible();
    }
  );

  test(
    "Botón 'Añadir Centro' visible",
    { tag: ["@high", "@e2e", "@centros", "@CENT-E2E-003"] },
    async ({ page }) => {
      const centrosPage = new CentrosPage(page);
      await centrosPage.loginAndGoto();
      await expect(centrosPage.addButton).toBeVisible();
    }
  );

  test(
    "Caja de búsqueda de centros presente y usable",
    { tag: ["@medium", "@e2e", "@centros", "@CENT-E2E-004"] },
    async ({ page }) => {
      const centrosPage = new CentrosPage(page);
      await centrosPage.loginAndGoto();
      await expect(centrosPage.searchBox).toBeVisible();
      await centrosPage.searchBox.fill("Busqueda test");
      await expect(centrosPage.searchBox).toHaveValue("Busqueda test");
    }
  );

  test(
    "Tabla de centros tiene columna 'Nombre' en cabecera",
    { tag: ["@medium", "@e2e", "@centros", "@CENT-E2E-005"] },
    async ({ page }) => {
      const centrosPage = new CentrosPage(page);
      await centrosPage.loginAndGoto();
      await expect(centrosPage.tablehead).toContainText("Nombre");
      await expect(centrosPage.tablehead).toContainText("Tipo");
      await expect(centrosPage.tablehead).toContainText("Ciudad");
    }
  );

  test(
    "API GET /api/centros devuelve array JSON",
    { tag: ["@critical", "@e2e", "@centros", "@CENT-E2E-006"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/centros");
      expect(response.status()).toBe(200);
      const body = await response.json();
      expect(Array.isArray(body)).toBe(true);
    }
  );

  test(
    "API POST /api/centros crea un centro y devuelve el objeto creado",
    { tag: ["@critical", "@e2e", "@centros", "@CENT-E2E-007"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);

      const centroData = {
        nombre: "Centro E2E Test",
        tipo: "Productor",
      };
      const response = await page.request.post("/api/centros", {
        data: centroData,
        headers: { "Content-Type": "application/json" },
      });
      expect([200, 201]).toContain(response.status());
      const body = await response.json();
      expect(body.nombre).toBe("Centro E2E Test");
      expect(body.id).toBeTruthy();

      // Cleanup
      await page.request.delete(`/api/centros/${body.id}`);
    }
  );

  test(
    "API PUT /api/centros/{id} actualiza un centro existente",
    { tag: ["@high", "@e2e", "@centros", "@CENT-E2E-008"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);

      // Create
      const created = await page.request.post("/api/centros", {
        data: { nombre: "Centro Update Test", tipo: "Gestor" },
        headers: { "Content-Type": "application/json" },
      });
      const centro = await created.json();

      // Update
      const updated = await page.request.put(`/api/centros/${centro.id}`, {
        data: { nombre: "Centro Actualizado", tipo: "Gestor" },
        headers: { "Content-Type": "application/json" },
      });
      expect([200, 204]).toContain(updated.status());

      // Cleanup
      await page.request.delete(`/api/centros/${centro.id}`);
    }
  );

  test(
    "API DELETE /api/centros/{id} elimina un centro creado",
    { tag: ["@high", "@e2e", "@centros", "@CENT-E2E-009"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);

      const created = await page.request.post("/api/centros", {
        data: { nombre: "Centro a Borrar", tipo: "Transportista" },
        headers: { "Content-Type": "application/json" },
      });
      const centro = await created.json();

      const deleteResponse = await page.request.delete(`/api/centros/${centro.id}`);
      expect([200, 204]).toContain(deleteResponse.status());
    }
  );
});
