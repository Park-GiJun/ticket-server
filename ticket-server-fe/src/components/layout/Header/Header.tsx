import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import {
  useAuthStore,
  useIsAuthenticated,
  useIsAdmin,
} from '../../../store/authStore';
import styles from './Header.module.css';

const NAV_ITEMS = [
  { to: '/', label: '홈', end: true },
  { to: '/events', label: '공연/전시', end: false },
];

export function Header() {
  const navigate = useNavigate();
  const authed = useIsAuthenticated();
  const isAdmin = useIsAdmin();
  const clearAuth = useAuthStore((s) => s.clearAuth);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [keyword, setKeyword] = useState('');

  useEffect(() => {
    if (!drawerOpen) return;
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = prev;
    };
  }, [drawerOpen]);

  const closeDrawer = () => setDrawerOpen(false);

  const onSearch = (e: FormEvent) => {
    e.preventDefault();
    const q = keyword.trim();
    closeDrawer();
    navigate(q ? `/events?q=${encodeURIComponent(q)}` : '/events');
  };

  const onLogout = () => {
    clearAuth();
    closeDrawer();
    navigate('/');
  };

  return (
    <header className={styles.header}>
      <div className={`container ${styles.inner}`}>
        <Link to="/" className={styles.logo} onClick={closeDrawer}>
          TICKET
        </Link>

        <nav className={styles.nav} aria-label="주요 메뉴">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                [styles.navLink, isActive ? styles.navLinkActive : '']
                  .filter(Boolean)
                  .join(' ')
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <form className={styles.search} onSubmit={onSearch} role="search">
          <input
            className={styles.searchInput}
            type="search"
            placeholder="공연, 아티스트 검색"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            aria-label="검색"
          />
        </form>

        <div className={styles.actions}>
          {authed ? (
            <>
              {isAdmin && (
                <Link to="/admin" className={styles.actionLink}>
                  관리자
                </Link>
              )}
              <Link to="/mypage" className={styles.actionLink}>
                마이페이지
              </Link>
              <button
                type="button"
                className={styles.logoutBtn}
                onClick={onLogout}
              >
                로그아웃
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className={styles.actionLink}>
                로그인
              </Link>
              <Link to="/signup" className={styles.signupBtn}>
                회원가입
              </Link>
            </>
          )}
        </div>

        <button
          type="button"
          className={styles.hamburger}
          aria-label="메뉴 열기"
          aria-expanded={drawerOpen}
          onClick={() => setDrawerOpen((v) => !v)}
        >
          <span />
          <span />
          <span />
        </button>
      </div>

      {drawerOpen && (
        <div className={styles.drawerOverlay} onClick={closeDrawer}>
          <aside
            className={styles.drawer}
            onClick={(e) => e.stopPropagation()}
            aria-label="모바일 메뉴"
          >
            <form
              className={styles.drawerSearch}
              onSubmit={onSearch}
              role="search"
            >
              <input
                className={styles.searchInput}
                type="search"
                placeholder="공연, 아티스트 검색"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                aria-label="검색"
              />
            </form>
            <nav className={styles.drawerNav} aria-label="모바일 주요 메뉴">
              {NAV_ITEMS.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.end}
                  className={styles.drawerLink}
                  onClick={closeDrawer}
                >
                  {item.label}
                </NavLink>
              ))}
            </nav>
            <div className={styles.drawerActions}>
              {authed ? (
                <>
                  <Link
                    to="/mypage"
                    className={styles.drawerLink}
                    onClick={closeDrawer}
                  >
                    마이페이지
                  </Link>
                  <button
                    type="button"
                    className={styles.drawerLogout}
                    onClick={onLogout}
                  >
                    로그아웃
                  </button>
                </>
              ) : (
                <>
                  <Link
                    to="/login"
                    className={styles.drawerLink}
                    onClick={closeDrawer}
                  >
                    로그인
                  </Link>
                  <Link
                    to="/signup"
                    className={styles.drawerSignup}
                    onClick={closeDrawer}
                  >
                    회원가입
                  </Link>
                </>
              )}
            </div>
          </aside>
        </div>
      )}
    </header>
  );
}

export default Header;
