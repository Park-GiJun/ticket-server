import axios from 'axios';
import { useAuthStore } from '../store/authStore';
import { toast } from '../store/toastStore';

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
    } else if (status === 404) {
      // 미구현 API(예: reservation/payment) 호출 시
      toast.error('아직 개발되지 않은 기능입니다. (개발 예정)');
    } else if (status !== undefined) {
      const data = error?.response?.data as { message?: string } | undefined;
      toast.error(data?.message ?? '요청 처리 중 오류가 발생했어요.');
    } else {
      toast.error('서버에 연결할 수 없어요. 잠시 후 다시 시도해 주세요.');
    }
    return Promise.reject(error);
  }
);

export default api;
