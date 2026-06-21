import type {
  SeatStatus,
  TicketEventCategory,
  TicketEventStatus,
} from '../types/ticketEvent';
import type { ReservationStatus } from '../types/reservation';
import type { PaymentMethod, PaymentStatus } from '../types/payment';

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

const RESERVATION_STATUS_LABELS: Record<ReservationStatus, string> = {
  HELD: '좌석 선점',
  CONFIRMED: '예매 완료',
  CANCELLED: '취소됨',
  EXPIRED: '선점 만료',
};

export function reservationStatusLabel(status: ReservationStatus): string {
  return RESERVATION_STATUS_LABELS[status] ?? status;
}

const PAYMENT_METHOD_LABELS: Record<PaymentMethod, string> = {
  CARD: '신용·체크카드',
  KAKAO_PAY: '카카오페이',
  TOSS: '토스페이',
  BANK_TRANSFER: '계좌이체',
};

export function paymentMethodLabel(method: PaymentMethod): string {
  return PAYMENT_METHOD_LABELS[method] ?? method;
}

const PAYMENT_STATUS_LABELS: Record<PaymentStatus, string> = {
  PENDING: '결제 대기',
  APPROVED: '결제 완료',
  FAILED: '결제 실패',
  CANCELLED: '결제 취소',
  REFUNDED: '환불 완료',
};

export function paymentStatusLabel(status: PaymentStatus): string {
  return PAYMENT_STATUS_LABELS[status] ?? status;
}
