import { Link, useLocation } from 'react-router-dom';
import { Button, Card } from '../../components/ui';
import { formatPrice, paymentMethodLabel } from '../../lib/format';
import type { PaymentMethod } from '../../types/payment';
import styles from './PaymentResultPage.module.css';

interface ResultState {
  ok?: boolean;
  amount?: number;
  method?: PaymentMethod;
  eventName?: string;
  seatLabels?: string[];
}

export default function PaymentResultPage() {
  const location = useLocation();
  const state = (location.state as ResultState | null) ?? {};
  const ok = state.ok ?? false;

  return (
    <div className={`container ${styles.page}`}>
      <Card padding="lg" className={styles.card}>
        <div
          className={[styles.icon, ok ? styles.ok : styles.fail].join(' ')}
          aria-hidden="true"
        >
          {ok ? '✓' : '!'}
        </div>
        <h1 className={styles.title}>
          {ok ? '결제가 완료되었어요' : '결제에 실패했어요'}
        </h1>
        <p className={styles.desc}>
          {ok
            ? '예매가 정상적으로 완료되었습니다. 마이페이지에서 확인할 수 있어요.'
            : '결제가 처리되지 않았어요. 잠시 후 다시 시도해 주세요.'}
        </p>

        {(state.eventName || state.amount != null) && (
          <dl className={styles.summary}>
            {state.eventName && (
              <div className={styles.row}>
                <dt>공연</dt>
                <dd>{state.eventName}</dd>
              </div>
            )}
            {state.seatLabels && state.seatLabels.length > 0 && (
              <div className={styles.row}>
                <dt>좌석</dt>
                <dd>{state.seatLabels.join(', ')}</dd>
              </div>
            )}
            {state.method && (
              <div className={styles.row}>
                <dt>결제 수단</dt>
                <dd>{paymentMethodLabel(state.method)}</dd>
              </div>
            )}
            {state.amount != null && (
              <div className={`${styles.row} ${styles.total}`}>
                <dt>결제 금액</dt>
                <dd>{formatPrice(state.amount)}</dd>
              </div>
            )}
          </dl>
        )}

        <div className={styles.actions}>
          {ok ? (
            <>
              <Link to="/mypage" className={styles.actionLink}>
                <Button fullWidth size="lg">
                  예매 내역 보기
                </Button>
              </Link>
              <Link to="/events" className={styles.actionLink}>
                <Button fullWidth size="lg" variant="secondary">
                  공연 더 보기
                </Button>
              </Link>
            </>
          ) : (
            <Link to="/events" className={styles.actionLink}>
              <Button fullWidth size="lg" variant="secondary">
                공연 목록으로
              </Button>
            </Link>
          )}
        </div>
      </Card>
    </div>
  );
}
