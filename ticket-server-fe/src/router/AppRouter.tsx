import { lazy, Suspense } from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { Layout } from '../components/layout/Layout/Layout';
import { ProtectedRoute } from '../components/layout/ProtectedRoute/ProtectedRoute';
import { Spinner } from '../components/ui';
import styles from './AppRouter.module.css';

const HomePage = lazy(() => import('../pages/home/HomePage'));
const EventListPage = lazy(() => import('../pages/events/EventListPage'));
const EventDetailPage = lazy(
  () => import('../pages/eventDetail/EventDetailPage')
);
const BookingPage = lazy(() => import('../pages/booking/BookingPage'));
const LoginPage = lazy(() => import('../pages/auth/LoginPage'));
const SignupPage = lazy(() => import('../pages/auth/SignupPage'));
const MyPage = lazy(() => import('../pages/mypage/MyPage'));
const NotFoundPage = lazy(() => import('../pages/NotFoundPage'));

function RouteFallback() {
  return (
    <div className={styles.fallback}>
      <Spinner size={36} />
    </div>
  );
}

export function AppRouter() {
  return (
    <BrowserRouter>
      <Suspense fallback={<RouteFallback />}>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/events" element={<EventListPage />} />
            <Route path="/events/:eventId" element={<EventDetailPage />} />
            <Route
              path="/events/:eventId/booking"
              element={<BookingPage />}
            />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />
            <Route
              path="/mypage"
              element={
                <ProtectedRoute>
                  <MyPage />
                </ProtectedRoute>
              }
            />
            <Route path="*" element={<NotFoundPage />} />
          </Route>
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
}

export default AppRouter;
