import React, { createContext, useState, useEffect, ReactNode } from "react";
import { loginApi, registerApi, refreshApi } from "../api/authApi";

interface AuthTokens {
  token: string;
  username: string;
  loginTime?: number;
}

interface AuthContextType {
  authTokens: AuthTokens | null;
  login: (credentials: { username: string; password: string }) => Promise<{ success: boolean; error?: string }>;
  register: (details: { username: string; email: string; password: string }) => Promise<{ success: boolean; error?: string }>;
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
      const loginTime = new Date().getTime();
      const tokenData = { ...response.data, loginTime };
      setAuthTokens(response.data);
      localStorage.setItem("authTokens", JSON.stringify(response.data));
      return { success: true };
    } catch (err: any) {
      console.error("Login failed", err);
      let errorMessage = "Login failed. Please try again.";

      if (err.response?.status === 401) {
        errorMessage = "Invalid username or password.";
      } else if (err.response?.status === 400) {
        errorMessage = "Please check your credentials and try again.";
      }

      return { success: false, error: errorMessage };
    }
  };

  const register = async (details: { username: string; email: string; password: string }) => {
    try {
      await registerApi(details);
      return { success: true };
    } catch (err: any) {
      console.error("Registration failed", err);
      let errorMessage = "Registration failed. Please try again.";

      if (err.response?.status === 400) {
        // Check if the error response has specific field errors
        const responseData = err.response?.data;
        if (responseData?.message) {
          errorMessage = responseData.message;
        } else if (responseData?.errors) {
          // Handle validation errors
          const errors = Object.values(responseData.errors).join(", ");
          errorMessage = errors as string;
        } else {
          errorMessage = "Username or email might already be taken. Please try different ones.";
        }
      } else if (err.response?.status === 409) {
        errorMessage = "Username or email already exists. Please choose different ones.";
      }

      return { success: false, error: errorMessage };
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
        const now = new Date().getTime();
        const loginTime = authTokens.loginTime || now;
        const hours24 = 24 * 60 * 60 * 1000;

        if (now - loginTime >= hours24) {
          logout();
        }else{
            const interval = setInterval(refreshToken, 10 * 60 * 1000); // every 10 mins
            return () => clearInterval(interval);
        }
    }
  }, [authTokens]);

  return (
    <AuthContext.Provider value={{ authTokens, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthContext;