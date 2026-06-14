import { useQuery } from '@tanstack/react-query';
import { listEvents } from '../../api/ticketEvents';
import { HeroCarousel } from './sections/HeroCarousel';
import { CategoryGrid } from './sections/CategoryGrid';
import { EventSection } from './sections/EventSection';
import styles from './HomePage.module.css';

export function HomePage() {
  const openQuery = useQuery({
    queryKey: ['events', { status: 'OPEN' }],
    queryFn: () => listEvents({ status: 'OPEN' }),
  });

  const scheduledQuery = useQuery({
    queryKey: ['events', { status: 'SCHEDULED' }],
    queryFn: () => listEvents({ status: 'SCHEDULED' }),
  });

  const allQuery = useQuery({
    queryKey: ['events', 'all'],
    queryFn: () => listEvents(),
  });

  return (
    <div className={styles.page}>
      <div className="container">
        <div className={styles.heroWrap}>
          <HeroCarousel />
        </div>

        <section className={styles.categories} aria-label="카테고리">
          <CategoryGrid />
        </section>

        <div className={styles.sections}>
          <EventSection
            title="지금 예매중"
            subtitle="놓치기 전에 지금 바로 예매하세요"
            moreTo="/events?status=OPEN"
            events={openQuery.data}
            isLoading={openQuery.isLoading}
            isError={openQuery.isError}
            limit={10}
            emptyTitle="예매중인 이벤트가 없어요"
            emptyDescription="곧 새로운 이벤트가 오픈될 예정이에요."
          />

          <EventSection
            title="오픈 예정"
            subtitle="오픈 알림을 받고 선예매를 준비하세요"
            moreTo="/events?status=SCHEDULED"
            events={scheduledQuery.data}
            isLoading={scheduledQuery.isLoading}
            isError={scheduledQuery.isError}
            limit={10}
            emptyTitle="오픈 예정 이벤트가 없어요"
            emptyDescription="새로운 오픈 일정을 준비 중이에요."
          />

          <EventSection
            title="전체 랭킹"
            subtitle="지금 가장 인기 있는 이벤트"
            moreTo="/events"
            events={allQuery.data}
            isLoading={allQuery.isLoading}
            isError={allQuery.isError}
            scroll
            limit={12}
            emptyTitle="등록된 이벤트가 없어요"
            emptyDescription="첫 번째 이벤트를 기다려 주세요."
          />
        </div>
      </div>
    </div>
  );
}

export default HomePage;
