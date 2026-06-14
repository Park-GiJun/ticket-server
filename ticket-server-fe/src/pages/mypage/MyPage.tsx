import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
  Badge,
  Button,
  Card,
  EmptyState,
  Skeleton,
} from '../../components/ui';
import type { BadgeTone } from '../../components/ui';
import { getMeApi } from '../../api/auth';
import { useAuthStore } from '../../store/authStore';
import { formatDate } from '../../lib/format';
import type { UserRole, UserStatus } from '../../types/auth';
import styles from './MyPage.module.css';

const ROLE_LABELS: Record<UserRole, string> = {
  USER: '일반 회원',
  ADMIN: '관리자',
};

const STATUS_LABELS: Record<UserStatus, string> = {
  ACTIVE: '활성',
  INACTIVE: '비활성',
  LOCKED: '잠김',
};

const STATUS_TONES: Record<UserStatus, BadgeTone> = {
  ACTIVE: 'success',
  INACTIVE: 'neutral',
  LOCKED: 'danger',
};

function ProfileSkeleton() {
  return (
    <Card padding="lg" className={styles.profileCard}>
      <div className={styles.profileHead}>
        <Skeleton width={64} height={64} radius="var(--radius-full)" />
        <div className={styles.profileHeadText}>
          <Skeleton width={140} height={22} />
          <Skeleton width={200} height={16} />
        </div>
      </div>
      <div className={styles.infoList}>
        {[0, 1, 2].map((i) => (
          <div className={styles.infoRow} key={i}>
            <Skeleton width={72} height={16} />
            <Skeleton width={120} height={16} />
          </div>
        ))}
      </div>
    </Card>
  );
}

export default function MyPage() {
  const navigate = useNavigate();
  const clearAuth = useAuthStore((s) => s.clearAuth);

  const {
    data: user,
    isLoading,
    isError,
    refetch,
  } = useQuery({
    queryKey: ['me'],
    queryFn: getMeApi,
  });

  const handleLogout = () => {
    clearAuth();
    navigate('/login', { replace: true });
  };

  const initial = user?.name?.trim()?.charAt(0)?.toUpperCase() ?? '?';

  return (
    <div className={styles.page}>
      <div className="container">
        <header className={styles.header}>
          <h1 className={styles.pageTitle}>마이페이지</h1>
          <p className={styles.pageSubtitle}>
            내 계정 정보와 예매 내역을 확인할 수 있어요.
          </p>
        </header>

        <div className={styles.grid}>
          <section className={styles.profileColumn} aria-label="내 정보">
            {isLoading ? (
              <ProfileSkeleton />
            ) : isError || !user ? (
              <Card padding="lg" className={styles.profileCard}>
                <EmptyState
                  icon="⚠️"
                  title="정보를 불러오지 못했어요"
                  description="잠시 후 다시 시도해 주세요."
                  action={
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={() => refetch()}
                    >
                      다시 시도
                    </Button>
                  }
                />
              </Card>
            ) : (
              <Card padding="lg" className={styles.profileCard}>
                <div className={styles.profileHead}>
                  <div className={styles.avatar} aria-hidden="true">
                    {initial}
                  </div>
                  <div className={styles.profileHeadText}>
                    <h2 className={styles.name}>{user.name}</h2>
                    <p className={styles.email}>{user.email}</p>
                  </div>
                </div>

                <dl className={styles.infoList}>
                  <div className={styles.infoRow}>
                    <dt className={styles.infoLabel}>역할</dt>
                    <dd className={styles.infoValue}>
                      <Badge tone={user.role === 'ADMIN' ? 'primary' : 'neutral'}>
                        {ROLE_LABELS[user.role] ?? user.role}
                      </Badge>
                    </dd>
                  </div>
                  <div className={styles.infoRow}>
                    <dt className={styles.infoLabel}>상태</dt>
                    <dd className={styles.infoValue}>
                      <Badge tone={STATUS_TONES[user.status] ?? 'neutral'}>
                        {STATUS_LABELS[user.status] ?? user.status}
                      </Badge>
                    </dd>
                  </div>
                  <div className={styles.infoRow}>
                    <dt className={styles.infoLabel}>가입일</dt>
                    <dd className={styles.infoValue}>
                      {user.createdAt ? formatDate(user.createdAt) : '-'}
                    </dd>
                  </div>
                </dl>

                <Button
                  variant="secondary"
                  fullWidth
                  onClick={handleLogout}
                  className={styles.logoutBtn}
                >
                  로그아웃
                </Button>
              </Card>
            )}
          </section>

          <section className={styles.historyColumn} aria-label="예매 내역">
            <Card padding="lg" className={styles.historyCard}>
              <h2 className={styles.sectionTitle}>예매 내역</h2>
              <EmptyState
                icon="🎫"
                title="준비 중이에요"
                description="예매 기능이 곧 열릴 예정이에요. 조금만 기다려 주세요."
              />
            </Card>
          </section>
        </div>
      </div>
    </div>
  );
}
