import { useNavigate } from 'react-router-dom';
import { Button, EmptyState } from '../components/ui';
import styles from './NotFoundPage.module.css';

export default function NotFoundPage() {
  const navigate = useNavigate();

  return (
    <div className={styles.page}>
      <div className="container">
        <div className={styles.inner}>
          <p className={styles.code} aria-hidden="true">
            404
          </p>
          <EmptyState
            icon="🔍"
            title="페이지를 찾을 수 없어요"
            description="주소가 바뀌었거나 더 이상 존재하지 않는 페이지예요."
            action={
              <Button
                size="lg"
                className={styles.homeBtn}
                onClick={() => navigate('/')}
              >
                홈으로 가기
              </Button>
            }
          />
        </div>
      </div>
    </div>
  );
}
