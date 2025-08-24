import axios from "axios";

const axiosInstance = axios.create({
  baseURL: "http://localhost:8080/api",
});

// Attach access token to every request (if available)
axiosInstance.interceptors.request.use(
  (config) => {
    const tokens = localStorage.getItem("authTokens");
    if (tokens) {
      const { token } = JSON.parse(tokens);
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Optional: Auto-handle token refresh on 401 responses
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const tokens = localStorage.getItem("authTokens");
        if (tokens) {
          const { token } = JSON.parse(tokens);
          // Call your refresh endpoint
          const refreshResponse = await axios.post("/api/auth/refresh", null, {
            headers: { Authorization: token },
          });

          const newTokens = refreshResponse.data;
          localStorage.setItem("authTokens", JSON.stringify(newTokens));

          // Update header and retry request
          originalRequest.headers.Authorization = `Bearer ${newTokens.token}`;
          return axiosInstance(originalRequest);
        }
      } catch (refreshError) {
        localStorage.removeItem("authTokens");
        window.location.href = "/login"; // force logout
      }
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
