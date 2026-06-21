export type ReservationStatus = 'HELD' | 'CONFIRMED' | 'CANCELLED' | 'EXPIRED';

export interface Reservation {
  id: number;
  userId: number;
  ticketEventId: number;
  sectionId: number;
  seatIds: number[];
  status: ReservationStatus;
  quantity: number;
  totalPrice: number;
  /** 좌석 점유(hold) 만료 시각 (ISO). status === 'HELD' 일 때만 의미 있음 */
  holdExpiresAt?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateReservationBody {
  ticketEventId: number;
  sectionId: number;
  seatIds: number[];
}
