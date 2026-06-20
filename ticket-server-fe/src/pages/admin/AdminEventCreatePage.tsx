import { useState } from 'react';
import type { CSSProperties, FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { createEvent } from '../../api/ticketEvents';
import type { TicketEventCategory } from '../../types/ticketEvent';
import { Button, Card } from '../../components/ui';
import { toast } from '../../store/toastStore';

const CATEGORIES: TicketEventCategory[] = [
  'CONCERT',
  'MUSICAL',
  'PLAY',
  'SPORTS',
  'EXHIBITION',
  'FESTIVAL',
  'ETC',
];

const fieldStyle: CSSProperties = { display: 'grid', gap: 6 };
const inputStyle: CSSProperties = {
  padding: '10px 12px',
  border: '1px solid var(--color-border, #ccc)',
  borderRadius: 8,
  font: 'inherit',
  background: 'var(--color-surface, #fff)',
  color: 'inherit',
};

function toIso(local: string): string {
  return new Date(local).toISOString();
}

export default function AdminEventCreatePage() {
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [category, setCategory] = useState<TicketEventCategory>('CONCERT');
  const [openAt, setOpenAt] = useState('');
  const [closedAt, setClosedAt] = useState('');
  const [eventAt, setEventAt] = useState('');

  const mutation = useMutation({
    mutationFn: () =>
      createEvent({
        ticketEventName: name.trim(),
        ticketOpenAt: toIso(openAt),
        ticketClosedAt: toIso(closedAt),
        ticketEventAt: toIso(eventAt),
        ticketEventCategory: category,
      }),
    onSuccess: (ev) => {
      toast.success('공연을 생성했어요. 구역·좌석을 추가하세요.');
      navigate(`/admin/events/${ev.id}`);
    },
  });

  const valid = name.trim() !== '' && openAt && closedAt && eventAt;

  function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (valid) mutation.mutate();
  }

  return (
    <div className="container" style={{ padding: '24px 0', maxWidth: 640 }}>
      <div style={{ marginBottom: 16 }}>
        <Link to="/admin" style={{ color: 'var(--color-text-muted,#888)' }}>
          ← 공연 관리로
        </Link>
        <h1 style={{ fontSize: 24, fontWeight: 700, marginTop: 8 }}>
          새 공연 만들기
        </h1>
      </div>

      <Card>
        <form onSubmit={handleSubmit} style={{ display: 'grid', gap: 16 }}>
          <label style={fieldStyle}>
            <span>공연명</span>
            <input
              style={inputStyle}
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="예: IU 월드투어 in 서울"
              required
            />
          </label>

          <label style={fieldStyle}>
            <span>카테고리</span>
            <select
              style={inputStyle}
              value={category}
              onChange={(e) =>
                setCategory(e.target.value as TicketEventCategory)
              }
            >
              {CATEGORIES.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </label>

          <label style={fieldStyle}>
            <span>예매 오픈</span>
            <input
              style={inputStyle}
              type="datetime-local"
              value={openAt}
              onChange={(e) => setOpenAt(e.target.value)}
              required
            />
          </label>

          <label style={fieldStyle}>
            <span>예매 마감</span>
            <input
              style={inputStyle}
              type="datetime-local"
              value={closedAt}
              onChange={(e) => setClosedAt(e.target.value)}
              required
            />
          </label>

          <label style={fieldStyle}>
            <span>공연 일시</span>
            <input
              style={inputStyle}
              type="datetime-local"
              value={eventAt}
              onChange={(e) => setEventAt(e.target.value)}
              required
            />
          </label>

          <Button type="submit" size="lg" loading={mutation.isPending} disabled={!valid}>
            공연 생성
          </Button>
        </form>
      </Card>
    </div>
  );
}
