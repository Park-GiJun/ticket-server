import { useCallback, useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import styles from './HeroCarousel.module.css';

interface HeroSlide {
  id: number;
  badge: string;
  title: string;
  subtitle: string;
  to: string;
  variant: 'a' | 'b' | 'c';
}

const SLIDES: HeroSlide[] = [
  {
    id: 1,
    badge: 'NEW OPEN',
    title: '지금 가장 뜨거운\n공연을 만나보세요',
    subtitle: '연보라빛 무대 위, 잊지 못할 순간',
    to: '/events?status=OPEN',
    variant: 'a',
  },
  {
    id: 2,
    badge: 'COMING SOON',
    title: '오픈 예정 티켓\n미리 확인하기',
    subtitle: '놓치면 후회하는 단독 선예매',
    to: '/events?status=SCHEDULED',
    variant: 'b',
  },
  {
    id: 3,
    badge: 'TICKET',
    title: '콘서트부터 스포츠까지\n한 곳에서',
    subtitle: '카테고리별 추천 이벤트를 둘러보세요',
    to: '/events',
    variant: 'c',
  },
];

const AUTOPLAY_MS = 5000;

export function HeroCarousel() {
  const [index, setIndex] = useState(0);
  const timer = useRef<ReturnType<typeof setInterval> | null>(null);

  const goTo = useCallback((next: number) => {
    setIndex((next + SLIDES.length) % SLIDES.length);
  }, []);

  const next = useCallback(() => goTo(index + 1), [goTo, index]);
  const prev = useCallback(() => goTo(index - 1), [goTo, index]);

  useEffect(() => {
    timer.current = setInterval(() => {
      setIndex((cur) => (cur + 1) % SLIDES.length);
    }, AUTOPLAY_MS);
    return () => {
      if (timer.current) clearInterval(timer.current);
    };
  }, [index]);

  return (
    <section className={styles.hero} aria-roledescription="carousel" aria-label="추천 배너">
      <div
        className={styles.track}
        style={{ transform: `translateX(-${index * 100}%)` }}
      >
        {SLIDES.map((slide, i) => (
          <Link
            key={slide.id}
            to={slide.to}
            className={styles.slide}
            data-variant={slide.variant}
            aria-hidden={i !== index}
            tabIndex={i === index ? 0 : -1}
            aria-label={`${slide.title.replace(/\n/g, ' ')} 바로가기`}
          >
            <div className={styles.slideInner}>
              <span className={styles.badge}>{slide.badge}</span>
              <h2 className={styles.title}>{slide.title}</h2>
              <p className={styles.subtitle}>{slide.subtitle}</p>
              <span className={styles.cta}>예매 바로가기 →</span>
            </div>
          </Link>
        ))}
      </div>

      <button
        type="button"
        className={`${styles.arrow} ${styles.arrowPrev}`}
        onClick={prev}
        aria-label="이전 배너"
      >
        ‹
      </button>
      <button
        type="button"
        className={`${styles.arrow} ${styles.arrowNext}`}
        onClick={next}
        aria-label="다음 배너"
      >
        ›
      </button>

      <div className={styles.dots} role="tablist" aria-label="배너 선택">
        {SLIDES.map((slide, i) => (
          <button
            key={slide.id}
            type="button"
            role="tab"
            aria-selected={i === index}
            aria-label={`${i + 1}번 배너`}
            className={`${styles.dot} ${i === index ? styles.dotActive : ''}`}
            onClick={() => goTo(i)}
          />
        ))}
      </div>
    </section>
  );
}

export default HeroCarousel;
