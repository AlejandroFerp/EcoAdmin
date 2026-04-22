import { test, expect } from "@playwright/test";
import { UsersPage } from "./users-page";
import { LoginPage } from "../login/login-page";
import { ADMIN_CREDENTIALS } from "../helpers";

test.describe("Usuarios", () => {
  test(
    "Página de usuarios carga con título correcto",
    { tag: ["@critical", "@e2e", "@usuarios", "@USUA-E2E-001"] },
    async ({ page }) => {
      const usersPage = new UsersPage(page);
      await usersPage.loginAndGoto();
      await expect(page).toHaveURL(/\/users/);
      await expect(page).toHaveTitle("EcoAdmin — Usuarios");
    }
  );

  test(
    "Encabezado 'Usuarios del sistema' visible",
    { tag: ["@high", "@e2e", "@usuarios", "@USUA-E2E-002"] },
    async ({ page }) => {
      const usersPage = new UsersPage(page);
      await usersPage.loginAndGoto();
      await expect(page.getByText("Usuarios del sistema")).toBeVisible();
    }
  );

  test(
    "Botón 'Nuevo Usuario' visible en la página",
    { tag: ["@high", "@e2e", "@usuarios", "@USUA-E2E-003"] },
    async ({ page }) => {
      const usersPage = new UsersPage(page);
      await usersPage.loginAndGoto();
      await expect(usersPage.addButton).toBeVisible();
    }
  );

  test(
    "Tabla de usuarios tiene columnas Nombre, Email, Rol",
    { tag: ["@medium", "@e2e", "@usuarios", "@USUA-E2E-004"] },
    async ({ page }) => {
      const usersPage = new UsersPage(page);
      await usersPage.loginAndGoto();
      await expect(usersPage.tablehead).toContainText("Nombre");
      await expect(usersPage.tablehead).toContainText("Email");
      await expect(usersPage.tablehead).toContainText("Rol");
    }
  );

  test(
    "API GET /api/usuarios devuelve array con al menos el admin",
    { tag: ["@critical", "@e2e", "@usuarios", "@USUA-E2E-005"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      const response = await page.request.get("/api/usuarios");
      expect(response.status()).toBe(200);
      const body = await response.json();
      expect(Array.isArray(body)).toBe(true);
      expect(body.length).toBeGreaterThanOrEqual(1);
      const adminUser = body.find((u: { email: string }) => u.email === "admin@ecoadmin.com");
      expect(adminUser).toBeDefined();
    }
  );

  test(
    "API POST /api/usuarios crea un usuario nuevo",
    { tag: ["@critical", "@e2e", "@usuarios", "@USUA-E2E-006"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);

      const timestamp = Date.now();
      const userData = {
        nombre: "Usuario E2E",
        email: `e2e-${timestamp}@test.com`,
        password: "password123",
        rol: "PRODUCTOR",
      };
      const response = await page.request.post("/api/usuarios", {
        data: userData,
        headers: { "Content-Type": "application/json" },
      });
      expect([200, 201]).toContain(response.status());
      const body = await response.json();
      expect(body.id).toBeTruthy();

      // Cleanup
      await page.request.delete(`/api/usuarios/${body.id}`);
    }
  );

  test(
    "API DELETE /api/usuarios/{id} elimina el usuario creado",
    { tag: ["@high", "@e2e", "@usuarios", "@USUA-E2E-007"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);

      const timestamp = Date.now();
      const created = await page.request.post("/api/usuarios", {
        data: {
          nombre: "Usuario a Borrar",
          email: `borrar-${timestamp}@test.com`,
          password: "pass123",
          rol: "GESTOR",
        },
        headers: { "Content-Type": "application/json" },
      });
      const usuario = await created.json();
      const deleteResponse = await page.request.delete(`/api/usuarios/${usuario.id}`);
      expect([200, 204]).toContain(deleteResponse.status());
    }
  );
});
