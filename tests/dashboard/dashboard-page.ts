import { Page, Locator, expect } from "@playwright/test";
import { BasePage } from "../base-page";
import { LoginPage } from "../login/login-page";
import { ADMIN_CREDENTIALS } from "../helpers";

export class DashboardPage extends BasePage {
  readonly sidebarLogo: Locator;
  readonly navDashboard: Locator;
  readonly navCentros: Locator;
  readonly navResiduos: Locator;
  readonly navTraslados: Locator;
  readonly navUsuarios: Locator;
  readonly navDirecciones: Locator;

  constructor(page: Page) {
    super(page);
    this.sidebarLogo = page.locator("aside").getByText("EcoAdmin");
    this.navDashboard = page.getByRole("link", { name: "Dashboard" });
    this.navCentros = page.getByRole("link", { name: "Centros" });
    this.navResiduos = page.getByRole("link", { name: "Residuos" });
    this.navTraslados = page.getByRole("link", { name: "Traslados" });
    this.navUsuarios = page.getByRole("link", { name: "Usuarios" });
    this.navDirecciones = page.getByRole("link", { name: "Direcciones" });
  }

  async loginAndGoto(): Promise<void> {
    const loginPage = new LoginPage(this.page);
    await loginPage.goto();
    await loginPage.login(ADMIN_CREDENTIALS);
    await this.page.waitForURL(/\/public\/index/);
  }

  async expectSidebarVisible(): Promise<void> {
    await expect(this.sidebarLogo).toBeVisible();
  }

  async expectAllNavLinksVisible(): Promise<void> {
    await expect(this.navDashboard).toBeVisible();
    await expect(this.navCentros).toBeVisible();
    await expect(this.navResiduos).toBeVisible();
    await expect(this.navTraslados).toBeVisible();
    await expect(this.navUsuarios).toBeVisible();
    await expect(this.navDirecciones).toBeVisible();
  }
}
