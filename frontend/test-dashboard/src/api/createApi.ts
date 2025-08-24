import axiosInstance from "./axiosInstance";

export const createApi = (basePath: string) => ({
  get: (url: string, config?: any) => axiosInstance.get(`${basePath}${url}`, config),
  post: (url: string, data?: any, config?: any) => axiosInstance.post(`${basePath}${url}`, data, config),
  put: (url: string, data?: any, config?: any) => axiosInstance.put(`${basePath}${url}`, data, config),
  delete: (url: string, config?: any) => axiosInstance.delete(`${basePath}${url}`, config),
});