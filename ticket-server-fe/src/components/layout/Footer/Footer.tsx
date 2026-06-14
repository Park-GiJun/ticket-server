import styles from './Footer.module.css';

export function Footer() {
  return (
    <footer className={styles.footer}>
      <div className={`container ${styles.inner}`}>
        <div className={styles.brand}>
          <span className={styles.logo}>TICKET</span>
          <p className={styles.tagline}>
            가장 쉬운 티켓 예매, 티켓서버에서 시작하세요.
          </p>
        </div>
        <nav className={styles.links} aria-label="푸터 메뉴">
          <a href="/events">공연/전시</a>
          <a href="/login">로그인</a>
          <a href="/signup">회원가입</a>
        </nav>
        <p className={styles.copyright}>
          © {new Date().getFullYear()} TICKET. All rights reserved.
        </p>
      </div>
    </footer>
  );
}

export default Footer;
