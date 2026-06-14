import { useMemo } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
  getEvent,
  getSeatAvailability,
  getSections,
} from '../../api/ticketEvents';
import type {
  Section,
  TicketEventStatus,
} from '../../types/ticketEvent';
import type { BadgeTone } from '../../components/ui';
import { Badge, Button, Card, EmptyState, Skeleton, Tag } from '../../components/ui';
import {
  categoryLabel,
  formatDate,
  formatPrice,
  statusLabel,
} from '../../lib/format';
import styles from './EventDetailPage.module.css';

const STATUS_TONE: Record<TicketEventStatus, BadgeTone> = {
  SCHEDULED: 'warning',
  OPEN: 'primary',
  CLOSED: 'neutral',
  SOLD_OUT: 'danger',
  CANCELLED: 'danger',
  COMPLETED: 'neutral',
};

/** OPEN 이 아닐 때 예매 버튼 비활성 사유 */
function disabledReason(status: TicketEventStatus): string | null {
  switch (status) {
    case 'OPEN':
      return null;
    case 'SCHEDULED':
      return '아직 예매가 시작되지 않았어요. 오픈을 기다려 주세요.';
    case 'SOLD_OUT':
      return '모든 좌석이 매진되었어요.';
    case 'CLOSED':
      return '예매가 마감되었어요.';
    case 'CANCELLED':
      return '취소된 공연이에요.';
    case 'COMPLETED':
      return '이미 종료된 공연이에요.';
    default:
      return '현재 예매할 수 없는 상태예요.';
  }
}

export default function EventDetailPage() {
  const { eventId } = useParams<{ eventId: string }>();
  const navigate = useNavigate();
  const id = Number(eventId);
  const validId = Number.isFinite(id) && id > 0;

  const eventQuery = useQuery({
    queryKey: ['event', id],
    queryFn: () => getEvent(id),
    enabled: validId,
  });

  const sectionsQuery = useQuery({
    queryKey: ['event', id, 'sections'],
    queryFn: () => getSections(id),
    enabled: validId,
  });

  const availabilityQuery = useQuery({
    queryKey: ['event', id, 'availability'],
    queryFn: () => getSeatAvailability(id),
    enabled: validId,
  });

  const event = eventQuery.data;

  /** 가격 낮은 순으로 정렬한 구역 목록 */
  const sections = useMemo<Section[]>(() => {
    const data = sectionsQuery.data;
    if (!data) return [];
    return [...data].sort((a, b) => a.price - b.price);
  }, [sectionsQuery.data]);

  const availability = availabilityQuery.data;
  const available = availability?.available ?? 0;
  const total = availability?.total ?? 0;
  const ratio = total > 0 ? Math.round((available / total) * 100) : 0;

  /* ------------------------------- 로딩 상태 ------------------------------- */
  if (eventQuery.isLoading) {
    return (
      <div className={`container ${styles.page}`}>
        <div className={styles.layout} aria-hidden="true">
          <Skeleton className={styles.posterSkeleton} radius="var(--radius-xl)" />
          <div className={styles.infoSkeleton}>
            <Skeleton width="30%" height={24} />
            <Skeleton width="80%" height={36} />
            <Skeleton width="60%" height={20} />
            <Skeleton width="100%" height={120} />
            <Skeleton width="100%" height={88} />
            <Skeleton width="100%" height={56} radius="var(--radius-lg)" />
          </div>
        </div>
      </div>
    );
  }

  /* -------------------------------- 404/에러 ------------------------------- */
  if (!validId || eventQuery.isError || !event) {
    return (
      <div className={`container ${styles.page}`}>
        <EmptyState
          icon="🔍"
          title="공연을 찾을 수 없어요"
          description="삭제되었거나 잘못된 주소일 수 있어요."
          action={
            <Link to="/" className={styles.homeLink}>
              홈으로 가기
            </Link>
          }
        />
      </div>
    );
  }

  const reason = disabledReason(event.ticketEventStatus);
  const bookable = reason == null;

  return (
    <div className={`container ${styles.page}`}>
      <nav className={styles.breadcrumb} aria-label="이동 경로">
        <Link to="/events" className={styles.breadcrumbLink}>
          공연 목록
        </Link>
        <span className={styles.breadcrumbSep} aria-hidden="true">
          /
        </span>
        <span className={styles.breadcrumbCurrent}>{event.ticketEventName}</span>
      </nav>

      <div className={styles.layout}>
        {/* 좌측: 포스터 placeholder */}
        <div
          className={styles.poster}
          data-category={event.ticketEventCategory}
        >
          <span className={styles.posterCategory}>
            {categoryLabel(event.ticketEventCategory)}
          </span>
          <span className={styles.posterTitle}>{event.ticketEventName}</span>
        </div>

        {/* 우측: 정보 패널 */}
        <div className={styles.info}>
          <div className={styles.metaRow}>
            <Tag active>{categoryLabel(event.ticketEventCategory)}</Tag>
            <Badge tone={STATUS_TONE[event.ticketEventStatus]}>
              {statusLabel(event.ticketEventStatus)}
            </Badge>
          </div>

          <h1 className={styles.title}>{event.ticketEventName}</h1>

          {/* 일시 정보 */}
          <dl className={styles.schedule}>
            <div className={styles.scheduleItem}>
              <dt className={styles.scheduleLabel}>공연일시</dt>
              <dd className={styles.scheduleValue}>
                {formatDate(event.ticketEventAt)}
              </dd>
            </div>
            <div className={styles.scheduleItem}>
              <dt className={styles.scheduleLabel}>예매기간</dt>
              <dd className={styles.scheduleValue}>
                {formatDate(event.ticketOpenAt)} ~{' '}
                {formatDate(event.ticketClosedAt)}
              </dd>
            </div>
          </dl>

          {/* 구역별 가격표 */}
          <Card padding="none" className={styles.priceCard}>
            <div className={styles.priceHeader}>
              <h2 className={styles.sectionHeading}>구역별 가격</h2>
            </div>
            {sectionsQuery.isLoading ? (
              <div className={styles.priceSkeleton}>
                <Skeleton height={20} />
                <Skeleton height={20} />
                <Skeleton height={20} />
              </div>
            ) : sections.length === 0 ? (
              <p className={styles.priceEmpty}>등록된 구역 정보가 없어요.</p>
            ) : (
              <ul className={styles.priceList}>
                {sections.map((s) => (
                  <li key={s.id} className={styles.priceRow}>
                    <div className={styles.priceGrade}>
                      <span className={styles.gradeBadge}>{s.grade}</span>
                      <span className={styles.sectionName}>{s.sectionName}</span>
                    </div>
                    <span className={styles.capacity}>
                      {s.capacity.toLocaleString('ko-KR')}석
                    </span>
                    <span className={styles.price}>{formatPrice(s.price)}</span>
                  </li>
                ))}
              </ul>
            )}
          </Card>

          {/* 잔여석 게이지 */}
          <Card padding="md" className={styles.seatCard}>
            <div className={styles.seatHead}>
              <h2 className={styles.sectionHeading}>잔여석</h2>
              {availabilityQuery.isLoading ? (
                <Skeleton width={90} height={18} />
              ) : (
                <span className={styles.seatCount}>
                  <strong>{available.toLocaleString('ko-KR')}</strong>
                  <span className={styles.seatTotal}>
                    {' '}
                    / {total.toLocaleString('ko-KR')}석
                  </span>
                </span>
              )}
            </div>
            <div
              className={styles.gauge}
              role="progressbar"
              aria-valuenow={ratio}
              aria-valuemin={0}
              aria-valuemax={100}
              aria-label="잔여석 비율"
            >
              <div
                className={styles.gaugeFill}
                data-low={ratio <= 20 ? 'true' : undefined}
                style={{ width: `${ratio}%` }}
              />
            </div>
            <p className={styles.gaugeCaption}>
              {availabilityQuery.isError
                ? '잔여석 정보를 불러오지 못했어요.'
                : `전체 좌석의 ${ratio}% 가 예매 가능해요.`}
            </p>
          </Card>

          {/* 예매하기 CTA */}
          <div className={styles.cta}>
            <Button
              size="lg"
              fullWidth
              disabled={!bookable}
              onClick={() => navigate(`/events/${event.id}/booking`)}
            >
              {bookable ? '예매하기' : statusLabel(event.ticketEventStatus)}
            </Button>
            {reason && <p className={styles.ctaReason}>{reason}</p>}
          </div>
        </div>
      </div>
    </div>
  );
}
