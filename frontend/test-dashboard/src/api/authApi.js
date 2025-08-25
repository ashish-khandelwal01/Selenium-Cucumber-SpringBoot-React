import { createApi } from "./createApi";

const authApi = createApi("/auth");

export const loginApi = (credentials) =>
  authApi.post("/login", credentials);

export const registerApi = (details) =>
  authApi.post("/register", details);

export const refreshApi = (token) =>
  authApi.post("/refresh", null, {
    headers: { Authorization: token },
  });