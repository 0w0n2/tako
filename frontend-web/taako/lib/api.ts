import axios from "axios";
import { useAuthStore } from '@/stores/useAuthStore'

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// 요청 인터셉터
api.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;

    if (token) {
      config.headers.Authorization = token;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (originalRequest.url.includes("/v1/auth/token/refresh")) {
      return Promise.reject(error);
    }

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true; // 무한 루프 방지 플래그

      try {
          const newToken = await useAuthStore.getState().refreshAccessToken();
          if (newToken) {
            originalRequest.headers["Authorization"] = newToken;
            return api(originalRequest); // 실패한 요청 재실행
          }
      } catch (refreshError) {
          console.error("토큰 재발급 실패:", refreshError);
          // useAuthStore.getState().logout();
      }
    }
    return Promise.reject(error);
  }
);
export default api;