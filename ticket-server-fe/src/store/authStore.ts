import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User } from '../types/auth';

interface AuthState {
  token: string | null;
  user: User | null;
  setAuth: (token: string) => void;
  setUser: (user: User | null) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      user: null,
      setAuth: (token) => set({ token }),
      setUser: (user) => set({ user }),
      clearAuth: () => set({ token: null, user: null }),
    }),
    {
      name: 'ticket-server-auth',
    }
  )
);

/** 인증 여부 셀렉터. */
export const isAuthenticated = (state: AuthState): boolean => !!state.token;

/** React 컴포넌트용 훅 셀렉터. */
export const useIsAuthenticated = (): boolean =>
  useAuthStore((state) => !!state.token);
