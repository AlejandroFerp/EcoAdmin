import { test, expect } from "@playwright/test";
import { LoginPage } from "./login-page";
import { ADMIN_CREDENTIALS } from "../helpers";

test.describe("Login", () => {
  test(
    "Muestra la página de login al acceder sin autenticar",
    { tag: ["@critical", "@e2e", "@auth", "@LOGIN-E2E-001"] },
    async ({ page }) => {
      await page.goto("/");
      await expect(page).toHaveURL(/\/public\/login/);
      await expect(page).toHaveTitle(/EcoAdmin/);
    }
  );

  test(
    "Admin puede iniciar sesión correctamente",
    { tag: ["@critical", "@e2e", "@auth", "@LOGIN-E2E-002"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      await expect(page).toHaveURL(/\/public\/index/);
      await expect(page).toHaveTitle(/Dashboard/);
    }
  );

  test(
    "Muestra error con credenciales incorrectas",
    { tag: ["@high", "@e2e", "@auth", "@LOGIN-E2E-003"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login({ email: "wrong@ecoadmin.com", password: "wrongpass" });
      await expect(page).toHaveURL(/error/);
      await loginPage.expectErrorVisible();
    }
  );

  test(
    "Redirige al login si se intenta acceder al dashboard sin sesión",
    { tag: ["@high", "@e2e", "@auth", "@LOGIN-E2E-004"] },
    async ({ page }) => {
      await page.goto("/public/index");
      await expect(page).toHaveURL(/\/public\/login/);
    }
  );

  test(
    "Cerrar sesión redirige al login con mensaje de confirmación",
    { tag: ["@high", "@e2e", "@auth", "@LOGIN-E2E-005"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      await expect(page).toHaveURL(/\/public\/index/);

      await page.getByTitle("Cerrar sesion").click();
      await expect(page).toHaveURL(/\/public\/login/);
      await loginPage.expectLogoutMessageVisible();
    }
  );
});
