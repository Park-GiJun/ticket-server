export type TicketEventCategory =
  | 'CONCERT'
  | 'MUSICAL'
  | 'PLAY'
  | 'SPORTS'
  | 'EXHIBITION'
  | 'FESTIVAL'
  | 'ETC';

export type TicketEventStatus =
  | 'SCHEDULED'
  | 'OPEN'
  | 'CLOSED'
  | 'SOLD_OUT'
  | 'CANCELLED'
  | 'COMPLETED';

export type TicketCreationStatus =
  | 'EVENT_CREATED'
  | 'SECTION_CREATED'
  | 'SEAT_CREATED'
  | 'COMPLETED';

export type SeatStatus = 'AVAILABLE' | 'HELD' | 'SOLD' | 'BLOCKED';

export interface TicketEvent {
  id: number;
  ticketEventName: string;
  ticketOpenAt: string;
  ticketClosedAt: string;
  ticketEventAt: string;
  ticketEventStatus: TicketEventStatus;
  ticketCreationStatus: TicketCreationStatus;
  ticketEventCategory: TicketEventCategory;
  createdAt?: string;
  updatedAt?: string;
}

export interface Section {
  id: number;
  ticketEventId: number;
  sectionName: string;
  grade: string;
  price: number;
  capacity: number;
  /** 좌석 배치도 행당 좌석 수(레이아웃) */
  seatsPerRow: number;
}

export interface Seat {
  id: number;
  sectionId: number;
  ticketEventId: number;
  rowLabel: string;
  seatNumber: number;
  status: SeatStatus;
}

export interface SeatAvailability {
  ticketEventId: number;
  counts: Record<SeatStatus, number>;
  total: number;
  available: number;
}
