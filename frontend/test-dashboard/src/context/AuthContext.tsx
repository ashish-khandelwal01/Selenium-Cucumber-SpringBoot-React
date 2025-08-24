import React, { createContext, useState, useEffect, ReactNode } from "react";
import { loginApi, registerApi, refreshApi } from "../api/authApi";

interface AuthTokens {
  token: string;
  username: string;
}

interface AuthContextType {
  authTokens: AuthTokens | null;
  login: (credentials: { username: string; password: string }) => Promise<boolean>;
  register: (details: { username: string; password: string }) => Promise<boolean>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [authTokens, setAuthTokens] = useState<AuthTokens | null>(() =>
    localStorage.getItem("authTokens")
      ? JSON.parse(localStorage.getItem("authTokens") as string)
      : null
  );

  const login = async (credentials: { username: string; password: string }) => {
    try {
      const response = await loginApi(credentials);
      setAuthTokens(response.data);
      localStorage.setItem("authTokens", JSON.stringify(response.data));
      return true;
    } catch (err) {
      console.error("Login failed", err);
      return false;
    }
  };

  const register = async (details: { username: string; password: string }) => {
    try {
      await registerApi(details);
      return true;
    } catch (err) {
      console.error("Registration failed", err);
      return false;
    }
  };

  const logout = () => {
    setAuthTokens(null);
    localStorage.removeItem("authTokens");
  };

  const refreshToken = async () => {
    if (!authTokens?.token) return;
    try {
      const response = await refreshApi(authTokens.token);
      setAuthTokens(response.data);
      localStorage.setItem("authTokens", JSON.stringify(response.data));
    } catch (err) {
      logout();
    }
  };

  useEffect(() => {
    if (authTokens) {
      const interval = setInterval(refreshToken, 10 * 60 * 1000); // every 10 mins
      return () => clearInterval(interval);
    }
  }, [authTokens]);

  return (
    <AuthContext.Provider value={{ authTokens, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthContext;
