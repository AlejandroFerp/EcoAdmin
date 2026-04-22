import { Page, Locator, expect } from "@playwright/test";
import { BasePage } from "../base-page";

export interface LoginCredentials {
  email: string;
  password: string;
}

export class LoginPage extends BasePage {
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly submitButton: Locator;
  readonly errorMessage: Locator;
  readonly logoutMessage: Locator;

  constructor(page: Page) {
    super(page);
    this.emailInput = page.getByLabel("Email");
    this.passwordInput = page.getByLabel("Contraseña");
    this.submitButton = page.getByRole("button", { name: "Acceder" });
    this.errorMessage = page.getByText("Email o contraseña incorrectos.");
    this.logoutMessage = page.getByText("Sesión cerrada correctamente.");
  }

  async goto(): Promise<void> {
    await super.goto("/login");
  }

  async login(credentials: LoginCredentials): Promise<void> {
    await this.emailInput.fill(credentials.email);
    await this.passwordInput.fill(credentials.password);
    await this.submitButton.click();
    await this.page.waitForLoadState("domcontentloaded");
  }

  async expectErrorVisible(): Promise<void> {
    await expect(this.errorMessage).toBeVisible();
  }

  async expectLogoutMessageVisible(): Promise<void> {
    await expect(this.logoutMessage).toBeVisible();
  }
}
