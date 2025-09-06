import axios from "axios";

const axiosInstance = axios.create({
  baseURL: "http://localhost:8080/api",
});

// COMBINED REQUEST INTERCEPTOR - handles both auth and browser
axiosInstance.interceptors.request.use(
  (config) => {
    // Handle authentication token
    const tokens = localStorage.getItem("authTokens");
    if (tokens) {
      const { token } = JSON.parse(tokens);
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Handle browser selection
    const browser = localStorage.getItem('selectedBrowser') || 'chrome';
    config.headers['X-Browser-Type'] = browser;
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor for token refresh
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

          // IMPORTANT: Re-add browser header for retry request
          const browser = localStorage.getItem('selectedBrowser') || 'chrome';
          originalRequest.headers['X-Browser-Type'] = browser;

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