import { useState } from 'react';
import type { CSSProperties } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  completeSetup,
  createSections,
  generateSeats,
  getEvent,
  getSeatAvailability,
  getSections,
  openEvent,
} from '../../api/ticketEvents';
import type { CreateSectionBody } from '../../api/ticketEvents';
import { Badge, Button, Card, Spinner } from '../../components/ui';
import { toast } from '../../store/toastStore';
import { formatPrice } from '../../lib/format';

interface SectionRow {
  sectionName: string;
  grade: string;
  price: string;
  capacity: string;
  seatsPerRow: string;
}

const emptyRow = (): SectionRow => ({
  sectionName: '',
  grade: 'R',
  price: '',
  capacity: '',
  seatsPerRow: '20',
});

const inputStyle: CSSProperties = {
  padding: '8px 10px',
  border: '1px solid var(--color-border, #ccc)',
  borderRadius: 6,
  font: 'inherit',
  width: '100%',
  background: 'var(--color-surface, #fff)',
  color: 'inherit',
};

export default function AdminEventManagePage() {
  const { id: idParam } = useParams<{ id: string }>();
  const eventId = Number(idParam);
  const valid = Number.isFinite(eventId) && eventId > 0;
  const qc = useQueryClient();

  const eventQuery = useQuery({
    queryKey: ['event', eventId],
    queryFn: () => getEvent(eventId),
    enabled: valid,
  });
  const sectionsQuery = useQuery({
    queryKey: ['sections', eventId],
    queryFn: () => getSections(eventId),
    enabled: valid,
  });
  const availQuery = useQuery({
    queryKey: ['seat-availability', eventId],
    queryFn: () => getSeatAvailability(eventId),
    enabled: valid,
    retry: false,
  });

  const [rows, setRows] = useState<SectionRow[]>([emptyRow()]);

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ['event', eventId] });
    qc.invalidateQueries({ queryKey: ['sections', eventId] });
    qc.invalidateQueries({ queryKey: ['seat-availability', eventId] });
    qc.invalidateQueries({ queryKey: ['admin-events'] });
  };

  const sectionMut = useMutation({
    mutationFn: () => {
      const body: CreateSectionBody[] = rows
        .filter((r) => r.sectionName.trim() !== '')
        .map((r) => ({
          sectionName: r.sectionName.trim(),
          grade: r.grade.trim() || 'R',
          price: Number(r.price) || 0,
          capacity: Number(r.capacity) || 0,
          seatsPerRow: Number(r.seatsPerRow) || 20,
        }));
      return createSections(eventId, body);
    },
    onSuccess: () => {
      toast.success('구역을 추가했어요.');
      setRows([emptyRow()]);
      invalidate();
    },
  });

  const seatMut = useMutation({
    mutationFn: () => generateSeats(eventId),
    onSuccess: (r) => {
      toast.success(`좌석 ${r.createdSeatCount.toLocaleString('ko-KR')}석을 생성했어요.`);
      invalidate();
    },
  });

  const completeMut = useMutation({
    mutationFn: () => completeSetup(eventId),
    onSuccess: () => {
      toast.success('셋업을 완료했어요.');
      invalidate();
    },
  });

  const openMut = useMutation({
    mutationFn: () => openEvent(eventId),
    onSuccess: () => {
      toast.success('예매를 오픈했어요!');
      invalidate();
    },
  });

  if (!valid) return <div className="container" style={{ padding: 24 }}>잘못된 접근입니다.</div>;
  if (eventQuery.isLoading)
    return (
      <div className="container" style={{ padding: 48, textAlign: 'center' }}>
        <Spinner size={32} />
      </div>
    );
  if (eventQuery.isError || !eventQuery.data)
    return <div className="container" style={{ padding: 24 }}>공연을 불러오지 못했어요.</div>;

  const ev = eventQuery.data;
  const sections = sectionsQuery.data ?? [];
  const avail = availQuery.data;
  const rowBtn = (i: number) => (
    <Button
      variant="ghost"
      onClick={() => setRows((p) => p.filter((_, idx) => idx !== i))}
      disabled={rows.length <= 1}
    >
      ×
    </Button>
  );

  return (
    <div className="container" style={{ padding: '24px 0', maxWidth: 820 }}>
      <Link to="/admin" style={{ color: 'var(--color-text-muted,#888)' }}>
        ← 공연 관리로
      </Link>

      <header style={{ margin: '8px 0 20px' }}>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
          <h1 style={{ fontSize: 24, fontWeight: 700 }}>{ev.ticketEventName}</h1>
          <Badge tone={ev.ticketEventStatus === 'OPEN' ? 'primary' : 'neutral'}>
            {ev.ticketEventStatus}
          </Badge>
          <Badge tone="neutral">진행: {ev.ticketCreationStatus}</Badge>
        </div>
      </header>

      {/* (1) 구역 추가 */}
      <Card style={{ marginBottom: 16 }}>
        <h2 style={{ fontSize: 18, fontWeight: 600, marginBottom: 12 }}>① 구역 추가</h2>
        <div style={{ display: 'grid', gap: 8 }}>
          {rows.map((r, i) => (
            <div
              key={i}
              style={{
                display: 'grid',
                gridTemplateColumns: '2fr 0.8fr 1.1fr 1fr 1fr auto',
                gap: 8,
                alignItems: 'center',
              }}
            >
              <input
                style={inputStyle}
                placeholder="구역명 (예: A구역)"
                value={r.sectionName}
                onChange={(e) =>
                  setRows((p) => p.map((x, idx) => (idx === i ? { ...x, sectionName: e.target.value } : x)))
                }
              />
              <input
                style={inputStyle}
                placeholder="등급"
                value={r.grade}
                onChange={(e) =>
                  setRows((p) => p.map((x, idx) => (idx === i ? { ...x, grade: e.target.value } : x)))
                }
              />
              <input
                style={inputStyle}
                type="number"
                placeholder="가격"
                value={r.price}
                onChange={(e) =>
                  setRows((p) => p.map((x, idx) => (idx === i ? { ...x, price: e.target.value } : x)))
                }
              />
              <input
                style={inputStyle}
                type="number"
                placeholder="총 좌석수"
                value={r.capacity}
                onChange={(e) =>
                  setRows((p) => p.map((x, idx) => (idx === i ? { ...x, capacity: e.target.value } : x)))
                }
              />
              <input
                style={inputStyle}
                type="number"
                placeholder="행당 좌석"
                value={r.seatsPerRow}
                onChange={(e) =>
                  setRows((p) => p.map((x, idx) => (idx === i ? { ...x, seatsPerRow: e.target.value } : x)))
                }
              />
              {rowBtn(i)}
            </div>
          ))}
        </div>
        <div style={{ display: 'flex', gap: 8, marginTop: 12 }}>
          <Button variant="secondary" onClick={() => setRows((p) => [...p, emptyRow()])}>
            + 구역 추가
          </Button>
          <Button
            loading={sectionMut.isPending}
            disabled={rows.every((r) => r.sectionName.trim() === '')}
            onClick={() => sectionMut.mutate()}
          >
            저장
          </Button>
        </div>

        {sections.length > 0 && (
          <ul style={{ marginTop: 16, display: 'grid', gap: 6 }}>
            {sections.map((s) => (
              <li
                key={s.id}
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  padding: '8px 12px',
                  background: 'var(--color-surface-alt, #f6f6f6)',
                  borderRadius: 6,
                }}
              >
                <span>
                  {s.sectionName} <Badge tone="neutral">{s.grade}</Badge>
                </span>
                <span>
                  {formatPrice(s.price)} · {s.capacity}석 · 행당 {s.seatsPerRow}
                </span>
              </li>
            ))}
          </ul>
        )}
      </Card>

      {/* (2) 좌석 생성 */}
      <Card style={{ marginBottom: 16 }}>
        <h2 style={{ fontSize: 18, fontWeight: 600, marginBottom: 8 }}>② 좌석 생성</h2>
        <p style={{ color: 'var(--color-text-muted,#888)', marginBottom: 12 }}>
          구역 capacity 기준으로 좌석을 자동 생성합니다.
          {avail ? ` (현재 ${avail.total.toLocaleString('ko-KR')}석 / 예매가능 ${avail.available.toLocaleString('ko-KR')}석)` : ''}
        </p>
        <Button
          variant="secondary"
          loading={seatMut.isPending}
          disabled={sections.length === 0}
          onClick={() => seatMut.mutate()}
        >
          좌석 생성
        </Button>
      </Card>

      {/* (3) 셋업 완료 + 오픈 */}
      <Card>
        <h2 style={{ fontSize: 18, fontWeight: 600, marginBottom: 12 }}>③ 공개</h2>
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
          <Button variant="secondary" loading={completeMut.isPending} onClick={() => completeMut.mutate()}>
            셋업 완료
          </Button>
          <Button loading={openMut.isPending} onClick={() => openMut.mutate()}>
            예매 오픈
          </Button>
        </div>
      </Card>
    </div>
  );
}
