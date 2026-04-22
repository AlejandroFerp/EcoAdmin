import { test, expect } from "@playwright/test";
import { LoginPage } from "./login-page";
import { ADMIN_CREDENTIALS } from "../helpers";

test.describe("Login", () => {
  test(
    "Muestra la página de login al acceder sin autenticar",
    { tag: ["@critical", "@e2e", "@auth", "@LOGIN-E2E-001"] },
    async ({ page }) => {
      await page.goto("/");
      await expect(page).toHaveURL(/\/login/);
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
      await expect(page).toHaveURL(/\/dashboard/);
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
      await page.goto("/dashboard");
      await expect(page).toHaveURL(/\/login/);
    }
  );

  test(
    "Cerrar sesión redirige al login con mensaje de confirmación",
    { tag: ["@high", "@e2e", "@auth", "@LOGIN-E2E-005"] },
    async ({ page }) => {
      const loginPage = new LoginPage(page);
      await loginPage.goto();
      await loginPage.login(ADMIN_CREDENTIALS);
      await expect(page).toHaveURL(/\/dashboard/);

      await page.getByTitle("Cerrar sesion").click();
      await expect(page).toHaveURL(/\/login/);
      await loginPage.expectLogoutMessageVisible();
    }
  );

  test(
    "Página de login tiene título 'EcoAdmin — Acceso'",
    { tag: ["@medium", "@e2e", "@auth", "@LOGIN-E2E-006"] },
    async ({ page }) => {
      await page.goto("/login");
      await expect(page).toHaveTitle("EcoAdmin — Acceso");
    }
  );

  test(
    "Campo contraseña es de tipo password (enmascarado)",
    { tag: ["@medium", "@e2e", "@auth", "@LOGIN-E2E-007"] },
    async ({ page }) => {
      await page.goto("/login");
      const passwordInput = page.getByLabel("Contraseña");
      await expect(passwordInput).toHaveAttribute("type", "password");
    }
  );

  test(
    "Label 'Email' visible en formulario de login",
    { tag: ["@medium", "@e2e", "@auth", "@LOGIN-E2E-008"] },
    async ({ page }) => {
      await page.goto("/login");
      await expect(page.getByLabel("Email")).toBeVisible();
      await expect(page.getByRole("button", { name: "Acceder" })).toBeVisible();
    }
  );

  test(
    "El campo email tiene atributo required (validación HTML5)",
    { tag: ["@medium", "@e2e", "@auth", "@LOGIN-E2E-009"] },
    async ({ page }) => {
      await page.goto("/login");
      const emailInput = page.getByLabel("Email");
      await expect(emailInput).toHaveAttribute("required", "");
    }
  );

  test(
    "El campo email tiene placeholder con formato de correo",
    { tag: ["@low", "@e2e", "@auth", "@LOGIN-E2E-010"] },
    async ({ page }) => {
      await page.goto("/login");
      const emailInput = page.getByLabel("Email");
      await expect(emailInput).toHaveAttribute("placeholder", /empresa|@/);
    }
  );
});
