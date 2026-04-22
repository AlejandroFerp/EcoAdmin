import { Page, Locator, expect } from "@playwright/test";
import { BasePage } from "../base-page";
import { LoginPage } from "../login/login-page";
import { ADMIN_CREDENTIALS } from "../helpers";

export class DashboardPage extends BasePage {
  readonly sidebarLogo: Locator;
  readonly navDashboard: Locator;
  readonly navCenters: Locator;
  readonly navWaste: Locator;
  readonly navShipments: Locator;
  readonly navUsers: Locator;
  readonly navAddresses: Locator;

  constructor(page: Page) {
    super(page);
    this.sidebarLogo = page.locator("aside").getByText("EcoAdmin", { exact: true });
    this.navDashboard = page.getByRole("link", { name: "Dashboard" });
    this.navCenters = page.getByRole("link", { name: "Centros" });
    this.navWaste = page.getByRole("link", { name: "Residuos" });
    this.navShipments = page.getByRole("link", { name: "Traslados" });
    this.navUsers = page.getByRole("link", { name: "Usuarios" });
    this.navAddresses = page.getByRole("link", { name: "Direcciones" });
  }

  async loginAndGoto(): Promise<void> {
    const loginPage = new LoginPage(this.page);
    await loginPage.goto();
    await loginPage.login(ADMIN_CREDENTIALS);
    await this.page.waitForURL(/\/dashboard/);
  }

  async expectSidebarVisible(): Promise<void> {
    await expect(this.sidebarLogo).toBeVisible();
  }

  async expectAllNavLinksVisible(): Promise<void> {
    await expect(this.navDashboard).toBeVisible();
    await expect(this.navCenters).toBeVisible();
    await expect(this.navWaste).toBeVisible();
    await expect(this.navShipments).toBeVisible();
    await expect(this.navUsers).toBeVisible();
    await expect(this.navAddresses).toBeVisible();
  }
}
