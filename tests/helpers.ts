export const ADMIN_CREDENTIALS = {
  email: "admin@ecoadmin.com",
  password: "admin123",
} as const;

export const BASE_URL = "http://localhost:8080";

export const ROUTES = {
  login: "/login",
  dashboard: "/dashboard",
  centers: "/centers",
  waste: "/waste",
  shipments: "/shipments",
  users: "/users",
  addresses: "/addresses",
  logout: "/logout",
} as const;
