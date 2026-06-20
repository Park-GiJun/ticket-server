import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useIsAdmin, useIsAuthenticated } from '../../../store/authStore';

export interface AdminRouteProps {
  children: ReactNode;
}

/** 인증 + ADMIN role 가드. 미인증 → 로그인, 비관리자 → 홈. */
export function AdminRoute({ children }: AdminRouteProps) {
  const authed = useIsAuthenticated();
  const isAdmin = useIsAdmin();
  const location = useLocation();

  if (!authed) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  if (!isAdmin) {
    return <Navigate to="/" replace />;
  }
  return <>{children}</>;
}

export default AdminRoute;
