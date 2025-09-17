import api from "@/lib/api";

import { create } from "zustand";
// import { persist, createJSONStorage } from 'zustand/middleware' // persist사용해서 토큰 스토리지 저장하기

interface AuthState {
  isLoggedIn: boolean | null;
  loading: boolean;
  error: string | null;
  newToken: string | null;
  login: (email: string, password: string) => Promise<void>;
  refreshAccessToken: () => Promise<string | null>;
  logout: () => void;
  checkAuth: () => void;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  newToken: null,
  isLoggedIn: null,
  loading: false,
  error: null,

  checkAuth: async () => {
    if (typeof window === "undefined") return; // 브라우저에서만 실행
  
    const token = localStorage.getItem("accessToken");
  
    if (token) {
      // 토큰이 있으면 로그인 상태 유지
      set({ isLoggedIn: true });
    } else if (get().isLoggedIn) {
      // 토큰은 없지만 이전에 로그인 상태였으면 재발급 시도
      const newToken = await get().refreshAccessToken(); // 성공 시 token 반환, 실패 시 null
      if (newToken) {
        set({ isLoggedIn: true });
      } else {
        set({ isLoggedIn: false });
      }
    } else {
      // 토큰도 없고 로그인 상태도 아니면 로그아웃 처리
      set({ isLoggedIn: false });
    }
  },

  login: async (email: string, password: string) => {
    set({ loading: true, error: null });
    try {
      const res = await api.post("/v1/auth/sign-in", { email, password });
      if (res.data.code !== 200) {
        alert(res.data.message);
        set({ loading: false, isLoggedIn: false });
        return;
      }

      const token = res.headers["authorization"];
      localStorage.setItem("accessToken", token);
      alert("로그인에 성공했습니다.");
      set({ loading: false, isLoggedIn: true });
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "로그인 실패",
        loading: false,
        isLoggedIn: false,
      });
    }
  },

  logout: async () => {
    try {
      const token = localStorage.getItem("accessToken");
      if (!token) return;
  
      await api.post(
        "/v1/auth/sign-out",
        { withCredentials: true },
        { headers: { Authorization: token } }
      );
      localStorage.removeItem("accessToken");
      set({ isLoggedIn: false });
    } catch (err: any) {
      console.error("로그아웃 실패:", err);
    }
  },

  refreshAccessToken: async () => {
    try {
      const oldToken = localStorage.getItem("accessToken");
      const res = await api.post("/v1/auth/token/refresh", {}, {
        headers: { Authorization: oldToken },
        withCredentials: true,
      });
      console.log(res)
      const token = res.headers["authorization"];
      if (token) localStorage.setItem("accessToken", token);
      return token;
    } catch (err: any) {
      console.error("재요청 실패: ", err);
      return null;
    }
  },
}));