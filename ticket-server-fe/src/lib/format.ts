import type {
  SeatStatus,
  TicketEventCategory,
  TicketEventStatus,
} from '../types/ticketEvent';

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'];

const pad = (n: number): string => String(n).padStart(2, '0');

/** ISO 문자열 -> "YYYY.MM.DD(요일) HH:mm" */
export function formatDate(iso: string): string {
  if (!iso) return '';
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  const y = d.getFullYear();
  const mo = pad(d.getMonth() + 1);
  const day = pad(d.getDate());
  const w = WEEKDAYS[d.getDay()];
  const h = pad(d.getHours());
  const mi = pad(d.getMinutes());
  return `${y}.${mo}.${day}(${w}) ${h}:${mi}`;
}

/** 숫자 -> "12,000원" */
export function formatPrice(n: number): string {
  if (n == null || Number.isNaN(n)) return '';
  return `${n.toLocaleString('ko-KR')}원`;
}

const CATEGORY_LABELS: Record<TicketEventCategory, string> = {
  CONCERT: '콘서트',
  MUSICAL: '뮤지컬',
  PLAY: '연극',
  SPORTS: '스포츠',
  EXHIBITION: '전시',
  FESTIVAL: '페스티벌',
  ETC: '기타',
};

export function categoryLabel(cat: TicketEventCategory): string {
  return CATEGORY_LABELS[cat] ?? cat;
}

const STATUS_LABELS: Record<TicketEventStatus, string> = {
  SCHEDULED: '오픈예정',
  OPEN: '예매중',
  CLOSED: '마감',
  SOLD_OUT: '매진',
  CANCELLED: '취소',
  COMPLETED: '종료',
};

export function statusLabel(status: TicketEventStatus): string {
  return STATUS_LABELS[status] ?? status;
}

const SEAT_STATUS_LABELS: Record<SeatStatus, string> = {
  AVAILABLE: '선택가능',
  HELD: '선점됨',
  SOLD: '판매완료',
  BLOCKED: '판매불가',
};

export function seatStatusLabel(status: SeatStatus): string {
  return SEAT_STATUS_LABELS[status] ?? status;
}
