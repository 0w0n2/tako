import { create } from "zustand";
import api from "@/lib/api";

interface AuthState {
  isLoggedIn: boolean | null;
  loading: boolean;
  error: string | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  checkAuth: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  isLoggedIn: null,
  loading: false,
  error: null,

  checkAuth: () => {
    if (typeof window !== "undefined") { // 브라우저에서만 실행
      const token = localStorage.getItem("accessToken");
      set({ isLoggedIn: !!token });
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
}));