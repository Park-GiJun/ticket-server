import axios from 'axios';
import { useAuthStore } from '../store/authStore';
import { toast } from '../store/toastStore';
import { getErrorMessage } from '../lib/errors';

declare module 'axios' {
  // 배경 조회(예: 미구현 reservation/payment)에서 404 토스트를 끄기 위한 플래그
  export interface AxiosRequestConfig {
    silent404?: boolean;
  }
}

export const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status: number | undefined = error?.response?.status;
    if (status === 401) {
      useAuthStore.getState().clearAuth();
      const path = window.location.pathname;
      if (path !== '/login' && path !== '/signup') {
        toast.error('로그인이 필요합니다.');
        window.location.href = '/login';
      }
    } else if (status === 404 && error?.config?.silent404) {
      // 미구현 API(예: reservation/payment) 배경 조회는 조용히 무시.
    } else {
      toast.error(getErrorMessage(error));
    }
    return Promise.reject(error);
  }
);

export default api;
