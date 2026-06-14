import { Link } from 'react-router-dom';
import type { TicketEventCategory } from '../../../types/ticketEvent';
import { categoryLabel } from '../../../lib/format';
import styles from './CategoryGrid.module.css';

interface CategoryItem {
  category: TicketEventCategory;
  emoji: string;
}

const CATEGORIES: CategoryItem[] = [
  { category: 'CONCERT', emoji: '🎤' },
  { category: 'MUSICAL', emoji: '🎭' },
  { category: 'PLAY', emoji: '🎬' },
  { category: 'SPORTS', emoji: '⚽' },
  { category: 'EXHIBITION', emoji: '🖼️' },
  { category: 'FESTIVAL', emoji: '🎉' },
  { category: 'ETC', emoji: '✨' },
];

export function CategoryGrid() {
  return (
    <nav className={styles.grid} aria-label="카테고리 바로가기">
      {CATEGORIES.map(({ category, emoji }) => (
        <Link
          key={category}
          to={`/events?category=${category}`}
          className={styles.item}
          aria-label={`${categoryLabel(category)} 이벤트 보기`}
        >
          <span className={styles.iconWrap} aria-hidden="true">
            <span className={styles.icon}>{emoji}</span>
          </span>
          <span className={styles.label}>{categoryLabel(category)}</span>
        </Link>
      ))}
    </nav>
  );
}

export default CategoryGrid;
