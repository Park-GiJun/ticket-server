import { useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { listEvents } from '../../api/ticketEvents';
import type {
  TicketEvent,
  TicketEventCategory,
  TicketEventStatus,
} from '../../types/ticketEvent';
import { Tag, Skeleton, EmptyState, ErrorState } from '../../components/ui';
import EventCard from '../../components/event/EventCard/EventCard';
import { categoryLabel } from '../../lib/format';
import styles from './EventListPage.module.css';

/** 카테고리 칩 목록 (전체 + 7종) */
const CATEGORIES: TicketEventCategory[] = [
  'CONCERT',
  'MUSICAL',
  'PLAY',
  'SPORTS',
  'EXHIBITION',
  'FESTIVAL',
  'ETC',
];

/** 예매상태 필터: 전체 / 예매중 / 오픈예정 / 마감 */
interface StatusFilterOption {
  value: '' | TicketEventStatus;
  label: string;
}

const STATUS_FILTERS: StatusFilterOption[] = [
  { value: '', label: '전체' },
  { value: 'OPEN', label: '예매중' },
  { value: 'SCHEDULED', label: '오픈예정' },
  { value: 'CLOSED', label: '마감' },
];

function isCategory(v: string | null): v is TicketEventCategory {
  return v != null && (CATEGORIES as string[]).includes(v);
}

function isStatusFilter(v: string | null): v is TicketEventStatus {
  return (
    v != null &&
    STATUS_FILTERS.some((o) => o.value !== '' && o.value === v)
  );
}

export default function EventListPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  const categoryParam = searchParams.get('category');
  const statusParam = searchParams.get('status');

  const category = isCategory(categoryParam) ? categoryParam : undefined;
  const status = isStatusFilter(statusParam) ? statusParam : undefined;

  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['events', { category, status }],
    queryFn: () => listEvents({ category, status }),
  });

  /** 최신 이벤트(ticketEventAt 가까운 순)로 정렬 */
  const events = useMemo<TicketEvent[]>(() => {
    if (!data) return [];
    return [...data].sort(
      (a, b) =>
        new Date(a.ticketEventAt).getTime() -
        new Date(b.ticketEventAt).getTime()
    );
  }, [data]);

  const updateParam = (key: 'category' | 'status', value: string | null) => {
    const next = new URLSearchParams(searchParams);
    if (value == null || value === '') {
      next.delete(key);
    } else {
      next.set(key, value);
    }
    setSearchParams(next, { replace: true });
  };

  const handleCategoryClick = (value: TicketEventCategory) => {
    // 같은 칩을 다시 누르면 해제(전체)
    updateParam('category', category === value ? null : value);
  };

  return (
    <div className={`container ${styles.page}`}>
      <header className={styles.header}>
        <h1 className={styles.heading}>공연 둘러보기</h1>
        <p className={styles.subheading}>
          관심 있는 카테고리와 예매 상태로 공연을 골라보세요.
        </p>
      </header>

      {/* 카테고리 칩 행 */}
      <nav className={styles.chipRow} aria-label="카테고리 필터">
        <Tag active={!category} onClick={() => updateParam('category', null)}>
          전체
        </Tag>
        {CATEGORIES.map((cat) => (
          <Tag
            key={cat}
            active={category === cat}
            onClick={() => handleCategoryClick(cat)}
          >
            {categoryLabel(cat)}
          </Tag>
        ))}
      </nav>

      {/* 예매상태 필터 + 개수 */}
      <div className={styles.controls}>
        <div className={styles.statusRow} role="group" aria-label="예매 상태 필터">
          {STATUS_FILTERS.map((opt) => {
            const active =
              opt.value === '' ? !status : status === opt.value;
            return (
              <button
                key={opt.label}
                type="button"
                className={`${styles.statusBtn} ${
                  active ? styles.statusActive : ''
                }`}
                aria-pressed={active}
                onClick={() => updateParam('status', opt.value || null)}
              >
                {opt.label}
              </button>
            );
          })}
        </div>
        {!isLoading && !isError && (
          <p className={styles.count} aria-live="polite">
            총 <strong>{events.length}</strong>건
          </p>
        )}
      </div>

      {/* 결과 영역 */}
      {isLoading ? (
        <div className={styles.grid} aria-hidden="true">
          {Array.from({ length: 8 }).map((_, i) => (
            <div key={i} className={styles.skeletonCard}>
              <Skeleton height={180} radius="var(--radius-lg)" />
              <div className={styles.skeletonBody}>
                <Skeleton width="40%" height={20} />
                <Skeleton width="80%" height={22} />
                <Skeleton width="60%" height={16} />
              </div>
            </div>
          ))}
        </div>
      ) : isError ? (
        <ErrorState
          title="목록을 불러오지 못했어요"
          onRetry={() => refetch()}
        />
      ) : events.length === 0 ? (
        <EmptyState
          icon="🔍"
          title="조건에 맞는 공연이 없어요"
          description="필터를 변경하거나 전체 공연을 둘러보세요."
          action={
            <button
              type="button"
              className={styles.retryBtn}
              onClick={() => setSearchParams(new URLSearchParams(), { replace: true })}
            >
              필터 초기화
            </button>
          }
        />
      ) : (
        <ul className={styles.grid}>
          {events.map((event) => (
            <li key={event.id} className={styles.gridItem}>
              <EventCard event={event} />
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
