import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { listEvents } from '../../api/ticketEvents';
import { Badge, Button, Card, EmptyState, Spinner } from '../../components/ui';
import type { BadgeTone } from '../../components/ui';
import type { TicketEventStatus } from '../../types/ticketEvent';

const STATUS_TONE: Record<TicketEventStatus, BadgeTone> = {
  SCHEDULED: 'neutral',
  OPEN: 'primary',
  CLOSED: 'neutral',
  SOLD_OUT: 'danger',
  CANCELLED: 'danger',
  COMPLETED: 'neutral',
};

export default function AdminEventsPage() {
  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['admin-events'],
    queryFn: () => listEvents(),
  });

  const events = data ?? [];

  return (
    <div className="container" style={{ padding: '24px 0' }}>
      <header
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: 20,
          gap: 12,
        }}
      >
        <h1 style={{ fontSize: 24, fontWeight: 700 }}>관리자 · 공연 관리</h1>
        <Link to="/admin/events/new">
          <Button>+ 새 공연 만들기</Button>
        </Link>
      </header>

      {isLoading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: 48 }}>
          <Spinner size={32} />
        </div>
      ) : isError ? (
        <EmptyState
          icon="⚠️"
          title="공연을 불러오지 못했어요"
          description="잠시 후 다시 시도해 주세요."
          action={
            <Button variant="secondary" onClick={() => refetch()}>
              다시 시도
            </Button>
          }
        />
      ) : events.length === 0 ? (
        <EmptyState
          icon="🎫"
          title="등록된 공연이 없어요"
          description="새 공연을 만들어 시드 데이터를 추가해 보세요."
          action={
            <Link to="/admin/events/new">
              <Button>새 공연 만들기</Button>
            </Link>
          }
        />
      ) : (
        <div style={{ display: 'grid', gap: 12 }}>
          {events.map((e) => (
            <Card key={e.id}>
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  gap: 12,
                }}
              >
                <div>
                  <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                    <strong style={{ fontSize: 16 }}>{e.ticketEventName}</strong>
                    <Badge tone={STATUS_TONE[e.ticketEventStatus]}>
                      {e.ticketEventStatus}
                    </Badge>
                    <Badge tone="neutral">{e.ticketEventCategory}</Badge>
                  </div>
                  <div style={{ color: 'var(--color-text-muted, #888)', fontSize: 13, marginTop: 4 }}>
                    #{e.id} · 진행상태 {e.ticketCreationStatus}
                  </div>
                </div>
                <Link to={`/admin/events/${e.id}`}>
                  <Button variant="secondary">관리</Button>
                </Link>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
