import { Link } from 'react-router-dom';
import type { TicketEvent } from '../../../types/ticketEvent';
import { EventCard } from '../../../components/event/EventCard/EventCard';
import { EmptyState, Skeleton } from '../../../components/ui';
import styles from './EventSection.module.css';

export interface EventSectionProps {
  title: string;
  subtitle?: string;
  moreTo?: string;
  events: TicketEvent[] | undefined;
  isLoading: boolean;
  isError: boolean;
  emptyTitle: string;
  emptyDescription?: string;
  /** 가로 스크롤 레이아웃 (랭킹/전체용) */
  scroll?: boolean;
  /** 보여줄 최대 개수 */
  limit?: number;
}

const SKELETON_COUNT = 5;

function CardSkeleton() {
  return (
    <div className={styles.skelCard} aria-hidden="true">
      <Skeleton height="100%" radius="var(--radius-lg)" className={styles.skelPoster} />
      <div className={styles.skelBody}>
        <Skeleton width="40%" height={14} />
        <Skeleton width="85%" height={18} />
        <Skeleton width="60%" height={14} />
      </div>
    </div>
  );
}

export function EventSection({
  title,
  subtitle,
  moreTo,
  events,
  isLoading,
  isError,
  emptyTitle,
  emptyDescription,
  scroll = false,
  limit,
}: EventSectionProps) {
  const listClass = scroll ? styles.scroll : styles.grid;
  const visible =
    events && typeof limit === 'number' ? events.slice(0, limit) : events;

  return (
    <section className={styles.section}>
      <header className={styles.head}>
        <div className={styles.heading}>
          <h2 className={styles.title}>{title}</h2>
          {subtitle && <p className={styles.subtitle}>{subtitle}</p>}
        </div>
        {moreTo && (
          <Link to={moreTo} className={styles.more}>
            전체보기 →
          </Link>
        )}
      </header>

      {isLoading ? (
        <div className={listClass} aria-busy="true">
          {Array.from({ length: SKELETON_COUNT }).map((_, i) => (
            <CardSkeleton key={i} />
          ))}
        </div>
      ) : isError ? (
        <EmptyState
          icon="⚠️"
          title="이벤트를 불러오지 못했어요"
          description="잠시 후 다시 시도해 주세요."
        />
      ) : !visible || visible.length === 0 ? (
        <EmptyState title={emptyTitle} description={emptyDescription} />
      ) : (
        <div className={listClass}>
          {visible.map((event) => (
            <div key={event.id} className={styles.cell}>
              <EventCard event={event} />
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

export default EventSection;
