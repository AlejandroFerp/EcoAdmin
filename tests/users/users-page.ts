import { Page, Locator } from "@playwright/test";
import { BasePage } from "../base-page";
import { LoginPage } from "../login/login-page";
import { ADMIN_CREDENTIALS } from "../helpers";

export class UsersPage extends BasePage {
  readonly addButton: Locator;
  readonly searchBox: Locator;
  readonly tableBody: Locator;
  readonly tablehead: Locator;

  constructor(page: Page) {
    super(page);
    this.addButton = page.getByRole("button", { name: /Nuevo Usuario/ });
    this.searchBox = page.locator("#busqueda");
    this.tableBody = page.locator("#tablaCuerpo");
    this.tablehead = page.locator("table thead");
  }

  async loginAndGoto(): Promise<void> {
    const loginPage = new LoginPage(this.page);
    await loginPage.goto();
    await loginPage.login(ADMIN_CREDENTIALS);
    await this.page.goto("/users");
    await this.page.waitForLoadState("domcontentloaded");
  }
}
