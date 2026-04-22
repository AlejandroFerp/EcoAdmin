import { Page, Locator, expect } from "@playwright/test";
import { BasePage } from "../base-page";
import { LoginPage } from "../login/login-page";
import { ADMIN_CREDENTIALS } from "../helpers";

export class ShipmentsPage extends BasePage {
  readonly pageHeading: Locator;

  constructor(page: Page) {
    super(page);
    this.pageHeading = page.getByRole("heading", { name: "Recogidas" });
  }

  async loginAndGoto(): Promise<void> {
    const loginPage = new LoginPage(this.page);
    await loginPage.goto();
    await loginPage.login(ADMIN_CREDENTIALS);
    await this.page.goto("/shipments");
    await this.page.waitForLoadState("domcontentloaded");
  }
}
