export const ADMIN_CREDENTIALS = {
  email: "admin@ecoadmin.com",
  password: "admin123",
} as const;

export const BASE_URL = "http://localhost:8080";

export const ROUTES = {
  login: "/public/login",
  dashboard: "/public/index",
  centros: "/public/centros",
  residuos: "/public/residuos",
  traslados: "/public/traslados",
  usuarios: "/public/usuarios",
  direcciones: "/public/direcciones",
  logout: "/logout",
} as const;
