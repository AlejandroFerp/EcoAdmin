import { Page, Locator, expect } from "@playwright/test";
import { BasePage } from "../base-page";
import { LoginPage } from "../login/login-page";
import { ADMIN_CREDENTIALS } from "../helpers";

export class ReportsPage extends BasePage {
  readonly pageHeading: Locator;
  readonly generateButton: Locator;
  readonly csvButton: Locator;
  readonly pdfButton: Locator;
  readonly resultsTable: Locator;
  readonly summaryText: Locator;

  constructor(page: Page) {
    super(page);
    this.pageHeading = page.getByRole("heading", { name: /Informes/i });
    this.generateButton = page.getByRole("button", { name: "Generar informe" });
    this.csvButton = page.getByRole("button", { name: "Descargar CSV" });
    this.pdfButton = page.getByRole("button", { name: "Descargar PDF" });
    this.resultsTable = page.locator("#tablaCuerpo");
    this.summaryText = page.locator("#resumenInforme");
  }

  async loginAndGoto(): Promise<void> {
    const loginPage = new LoginPage(this.page);
    await loginPage.goto();
    await loginPage.login(ADMIN_CREDENTIALS);
    await this.page.goto("/reports");
    await this.page.waitForLoadState("domcontentloaded");
  }

  async selectReportTypeByText(text: string): Promise<void> {
    await this.page.getByRole("button", { name: text }).click();
  }

  async generateReport(): Promise<void> {
    await this.generateButton.click();
    await this.page.waitForTimeout(1500);
  }
}
