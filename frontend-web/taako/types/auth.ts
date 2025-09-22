export interface AuthState {
    token: string | null;
    loading: boolean;
    error: string | null;
    login: (email: string, password: string) => Promise<void>;
    refreshAccessToken: () => Promise<string | null>;
    logout: () => void;
  }