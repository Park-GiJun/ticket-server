import { useEffect, useState } from 'react';
import {
  Link,
  useLocation,
  useNavigate,
  useParams,
} from 'react-router-dom';
import { useMutation, useQuery } from '@tanstack/react-query';
import type { AxiosError } from 'axios';
import { getReservation } from '../../api/reservations';
import { createPayment } from '../../api/payments';
import { getEvent } from '../../api/ticketEvents';
import type { BookingSummary, PaymentMethod } from '../../types/payment';
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Skeleton,
} from '../../components/ui';
import { formatPrice, paymentMethodLabel } from '../../lib/format';
import styles from './PaymentPage.module.css';

const METHODS: PaymentMethod[] = ['CARD', 'KAKAO_PAY', 'TOSS', 'BANK_TRANSFER'];

/** holdExpiresAt 까지 남은 초. target 없으면 null. */
function useCountdown(target?: string): number | null {
  const [now, setNow] = useState(() => Date.now());
  useEffect(() => {
    if (!target) return;
    const timer = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(timer);
  }, [target]);
  if (!target) return null;
  return Math.floor((new Date(target).getTime() - now) / 1000);
}

function formatRemain(sec: number): string {
  const s = Math.max(0, sec);
  const m = Math.floor(s / 60);
  const r = s % 60;
  return `${String(m).padStart(2, '0')}:${String(r).padStart(2, '0')}`;
}

export default function PaymentPage() {
  const { reservationId: param } = useParams<{ reservationId: string }>();
  const reservationId = Number(param);
  const validId = Number.isFinite(reservationId) && reservationId > 0;

  const navigate = useNavigate();
  const location = useLocation();
  const summary = (location.state as { summary?: BookingSummary } | null)
    ?.summary;

  const [method, setMethod] = useState<PaymentMethod>('CARD');

  const reservationQuery = useQuery({
    queryKey: ['reservation', reservationId],
    queryFn: () => getReservation(reservationId),
    enabled: validId,
    retry: false,
  });
  const reservation = reservationQuery.data;

  const eventQuery = useQuery({
    queryKey: ['event', reservation?.ticketEventId],
    queryFn: () => getEvent(reservation!.ticketEventId),
    enabled: !!reservation,
  });
  const eventName = eventQuery.data?.ticketEventName;

  const remain = useCountdown(
    reservation?.status === 'HELD' ? reservation?.holdExpiresAt : undefined
  );
  const expired =
    reservation?.status === 'EXPIRED' || (remain !== null && remain <= 0);

  const amount = summary?.totalPrice ?? reservation?.totalPrice ?? 0;
  const quantity = summary?.quantity ?? reservation?.quantity ?? 0;
  const eventId = summary?.eventId ?? reservation?.ticketEventId;

  const payMutation = useMutation({
    mutationFn: createPayment,
    onSuccess: (payment) => {
      navigate('/payment/result', {
        replace: true,
        state: {
          ok: payment.status === 'APPROVED',
          payment,
          eventName,
          amount,
          method,
          seatLabels: summary?.seatLabels ?? [],
        },
      });
    },
    // 실패(네트워크/4xx/미구현)는 client 인터셉터가 토스트로 안내한다.
  });

  /* ------------------------------ 가드/상태 ------------------------------ */

  if (!validId) {
    return (
      <div className={`container ${styles.page}`}>
        <ErrorState
          title="잘못된 접근이에요"
          description="유효하지 않은 예약입니다."
          action={
            <Link to="/events">
              <Button variant="secondary">공연 목록으로</Button>
            </Link>
          }
        />
      </div>
    );
  }

  if (reservationQuery.isLoading) {
    return (
      <div className={`container ${styles.page}`}>
        <Card padding="lg">
          <Skeleton width="40%" height={24} />
          <div className={styles.skeletonRows}>
            <Skeleton height={56} radius="var(--radius-md)" />
            <Skeleton height={56} radius="var(--radius-md)" />
            <Skeleton height={120} radius="var(--radius-md)" />
          </div>
        </Card>
      </div>
    );
  }

  if (reservationQuery.isError || !reservation) {
    const status = (reservationQuery.error as AxiosError | null)?.response
      ?.status;
    // 예약/결제 백엔드 미구현(404) → 안내, 그 외 → 재시도
    if (status === 404) {
      return (
        <div className={`container ${styles.page}`}>
          <EmptyState
            icon="🚧"
            title="결제 기능 준비 중"
            description="예매·결제 백엔드(reservation/payment)가 완성되면 결제를 진행할 수 있어요."
            action={
              <Link to="/events">
                <Button variant="secondary">공연 목록으로</Button>
              </Link>
            }
          />
        </div>
      );
    }
    return (
      <div className={`container ${styles.page}`}>
        <ErrorState
          title="예약 정보를 불러오지 못했어요"
          onRetry={() => reservationQuery.refetch()}
        />
      </div>
    );
  }

  if (reservation.status === 'CONFIRMED') {
    return (
      <div className={`container ${styles.page}`}>
        <EmptyState
          icon="✅"
          title="이미 결제가 완료된 예약이에요"
          description="마이페이지에서 예매 내역을 확인할 수 있어요."
          action={
            <Link to="/mypage">
              <Button>마이페이지로</Button>
            </Link>
          }
        />
      </div>
    );
  }

  if (expired || reservation.status === 'CANCELLED') {
    return (
      <div className={`container ${styles.page}`}>
        <EmptyState
          icon="⌛"
          title="좌석 선점 시간이 만료되었어요"
          description="결제 시간이 지나 좌석 선점이 해제되었어요. 다시 예매해 주세요."
          action={
            <Link to={eventId ? `/events/${eventId}/booking` : '/events'}>
              <Button>다시 예매하기</Button>
            </Link>
          }
        />
      </div>
    );
  }

  /* -------------------------------- 렌더 -------------------------------- */

  return (
    <div className={`container ${styles.page}`}>
      <header className={styles.header}>
        <div>
          <p className={styles.eyebrow}>결제</p>
          <h1 className={styles.heading}>결제 정보를 확인해 주세요</h1>
        </div>
        {remain !== null && (
          <div className={styles.timer} role="timer" aria-live="polite">
            남은 시간 <strong>{formatRemain(remain)}</strong>
          </div>
        )}
      </header>

      <div className={styles.layout}>
        <div className={styles.main}>
          {/* 예매 요약 */}
          <Card>
            <h2 className={styles.sectionTitle}>예매 내역</h2>
            <dl className={styles.infoList}>
              <div className={styles.infoRow}>
                <dt>공연</dt>
                <dd>{eventName ?? `#${reservation.ticketEventId}`}</dd>
              </div>
              {summary?.sectionName && (
                <div className={styles.infoRow}>
                  <dt>구역</dt>
                  <dd>
                    {summary.sectionName}
                    {summary.grade && (
                      <Badge tone="primary" className={styles.gradeBadge}>
                        {summary.grade}
                      </Badge>
                    )}
                  </dd>
                </div>
              )}
              <div className={styles.infoRow}>
                <dt>좌석</dt>
                <dd>
                  {summary?.seatLabels?.length
                    ? summary.seatLabels.join(', ')
                    : `${quantity}석`}
                </dd>
              </div>
              <div className={styles.infoRow}>
                <dt>매수</dt>
                <dd>{quantity}매</dd>
              </div>
            </dl>
          </Card>

          {/* 결제 수단 */}
          <Card>
            <h2 className={styles.sectionTitle}>결제 수단</h2>
            <div className={styles.methods} role="radiogroup" aria-label="결제 수단">
              {METHODS.map((m) => {
                const active = m === method;
                return (
                  <button
                    key={m}
                    type="button"
                    role="radio"
                    aria-checked={active}
                    className={[
                      styles.method,
                      active ? styles.methodActive : '',
                    ]
                      .filter(Boolean)
                      .join(' ')}
                    onClick={() => setMethod(m)}
                  >
                    {paymentMethodLabel(m)}
                  </button>
                );
              })}
            </div>
          </Card>
        </div>

        {/* 결제 요약/CTA */}
        <aside className={styles.aside}>
          <Card className={styles.payCard}>
            <h2 className={styles.sectionTitle}>결제 금액</h2>
            <dl className={styles.summaryRows}>
              <div className={styles.summaryRow}>
                <dt>티켓 금액</dt>
                <dd>{formatPrice(amount)}</dd>
              </div>
              <div className={`${styles.summaryRow} ${styles.summaryTotal}`}>
                <dt>최종 결제 금액</dt>
                <dd>{formatPrice(amount)}</dd>
              </div>
            </dl>
            <Button
              fullWidth
              size="lg"
              disabled={payMutation.isPending}
              onClick={() =>
                payMutation.mutate({ reservationId, method })
              }
            >
              {payMutation.isPending
                ? '결제 중…'
                : `${formatPrice(amount)} 결제하기`}
            </Button>
            <p className={styles.notice}>
              결제는 테스트용 PG(FakePaymentGateway)로 처리됩니다.
            </p>
          </Card>
        </aside>
      </div>
    </div>
  );
}
