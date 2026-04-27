# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: addresses\addresses.spec.ts >> Direcciones >> API GET /api/direcciones devuelve array JSON
- Location: addresses\addresses.spec.ts:40:7

# Error details

```
Test timeout of 30000ms exceeded.
```

```
Error: locator.click: Test timeout of 30000ms exceeded.
Call log:
  - waiting for getByRole('button', { name: 'Acceder' })

```

# Page snapshot

```yaml
- generic [ref=e3]:
  - img "EcoAdmin" [ref=e5]
  - paragraph [ref=e6]: Bienvenido a
  - heading "EcoAdmin" [level=1] [ref=e7]
  - paragraph [ref=e8]: Inicia sesión con tu cuenta
  - generic [ref=e9]:
    - generic [ref=e10]:
      - generic:
        - img
      - textbox "Email" [ref=e11]:
        - /placeholder: " "
        - text: admin@ecoadmin.com
      - generic: Email
    - generic [ref=e12]:
      - generic:
        - img
      - textbox "Contraseña" [active] [ref=e13]:
        - /placeholder: " "
        - text: admin123
      - generic: Contraseña
    - button "Ingresar" [ref=e15] [cursor=pointer]
  - link "Registrarse" [ref=e17] [cursor=pointer]:
    - /url: /registro
  - paragraph [ref=e18]: EcoAdmin · Gestión de residuos peligrosos
```

# Test source

```ts
  1  | import { Page, Locator, expect } from "@playwright/test";
  2  | import { BasePage } from "../base-page";
  3  | 
  4  | export interface LoginCredentials {
  5  |   email: string;
  6  |   password: string;
  7  | }
  8  | 
  9  | export class LoginPage extends BasePage {
  10 |   readonly emailInput: Locator;
  11 |   readonly passwordInput: Locator;
  12 |   readonly submitButton: Locator;
  13 |   readonly errorMessage: Locator;
  14 |   readonly logoutMessage: Locator;
  15 | 
  16 |   constructor(page: Page) {
  17 |     super(page);
  18 |     this.emailInput = page.getByLabel("Email");
  19 |     this.passwordInput = page.getByLabel("Contraseña");
  20 |     this.submitButton = page.getByRole("button", { name: "Acceder" });
  21 |     this.errorMessage = page.getByText("Email o contraseña incorrectos.");
  22 |     this.logoutMessage = page.getByText("Sesión cerrada correctamente.");
  23 |   }
  24 | 
  25 |   async goto(): Promise<void> {
  26 |     await super.goto("/login");
  27 |   }
  28 | 
  29 |   async login(credentials: LoginCredentials): Promise<void> {
  30 |     await this.emailInput.fill(credentials.email);
  31 |     await this.passwordInput.fill(credentials.password);
> 32 |     await this.submitButton.click();
     |                             ^ Error: locator.click: Test timeout of 30000ms exceeded.
  33 |     await this.page.waitForLoadState("domcontentloaded");
  34 |   }
  35 | 
  36 |   async expectErrorVisible(): Promise<void> {
  37 |     await expect(this.errorMessage).toBeVisible();
  38 |   }
  39 | 
  40 |   async expectLogoutMessageVisible(): Promise<void> {
  41 |     await expect(this.logoutMessage).toBeVisible();
  42 |   }
  43 | }
  44 | 
```