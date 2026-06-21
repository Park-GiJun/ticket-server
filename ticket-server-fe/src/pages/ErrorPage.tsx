import { Button } from '../components/ui';
import styles from './ErrorPage.module.css';

export interface ErrorPageProps {
  /** ErrorBoundary 가 넘기는 상태 초기화 콜백. */
  onReset?: () => void;
  title?: string;
  description?: string;
}

/**
 * 전체 화면 에러 폴백. ErrorBoundary 의 fallback 으로 쓰이므로
 * 라우터 컨텍스트에 의존하지 않는다(`<a>` / window.location 사용).
 */
export default function ErrorPage({
  onReset,
  title = '문제가 발생했어요',
  description = '예상치 못한 오류로 화면을 표시할 수 없어요. 잠시 후 다시 시도해 주세요.',
}: ErrorPageProps) {
  return (
    <div className={styles.page}>
      <div className={styles.inner}>
        <div className={styles.icon} aria-hidden="true">
          ⚠️
        </div>
        <h1 className={styles.title}>{title}</h1>
        <p className={styles.desc}>{description}</p>
        <div className={styles.actions}>
          {onReset && (
            <Button size="lg" onClick={onReset}>
              다시 시도
            </Button>
          )}
          <Button
            variant="secondary"
            size="lg"
            onClick={() => window.location.assign('/')}
          >
            홈으로 가기
          </Button>
        </div>
      </div>
    </div>
  );
}
