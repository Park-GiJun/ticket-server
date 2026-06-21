export type PaymentMethod = 'CARD' | 'KAKAO_PAY' | 'TOSS' | 'BANK_TRANSFER';

export type PaymentStatus =
  | 'PENDING'
  | 'APPROVED'
  | 'FAILED'
  | 'CANCELLED'
  | 'REFUNDED';

export interface Payment {
  id: number;
  reservationId: number;
  amount: number;
  method: PaymentMethod;
  status: PaymentStatus;
  approvedAt?: string;
  createdAt: string;
}

export interface CreatePaymentBody {
  reservationId: number;
  method: PaymentMethod;
}

/** 예매 페이지 → 결제 페이지로 넘기는 화면 표시용 요약(라우터 state). */
export interface BookingSummary {
  eventId: number;
  sectionName: string;
  grade: string;
  seatLabels: string[];
  quantity: number;
  totalPrice: number;
}
