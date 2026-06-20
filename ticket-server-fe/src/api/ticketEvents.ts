import { api } from './client';
import type {
  Seat,
  SeatAvailability,
  Section,
  TicketEvent,
  TicketEventCategory,
  TicketEventStatus,
} from '../types/ticketEvent';

const BASE = '/ticket-events';

/* ------------------------------ 조회 (Query) ------------------------------ */

export async function listEvents(params?: {
  category?: TicketEventCategory;
  status?: TicketEventStatus;
}): Promise<TicketEvent[]> {
  const { data } = await api.get<TicketEvent[]>(BASE, { params });
  return data;
}

export async function getEvent(id: number): Promise<TicketEvent> {
  const { data } = await api.get<TicketEvent>(`${BASE}/${id}`);
  return data;
}

export async function getSections(eventId: number): Promise<Section[]> {
  const { data } = await api.get<Section[]>(`${BASE}/${eventId}/sections`);
  return data;
}

export async function getSection(
  eventId: number,
  sectionId: number
): Promise<Section> {
  const { data } = await api.get<Section>(
    `${BASE}/${eventId}/sections/${sectionId}`
  );
  return data;
}

export async function getSeats(eventId: number): Promise<Seat[]> {
  const { data } = await api.get<Seat[]>(`${BASE}/${eventId}/seats`);
  return data;
}

export async function getSeatAvailability(
  eventId: number
): Promise<SeatAvailability> {
  const { data } = await api.get<SeatAvailability>(
    `${BASE}/${eventId}/seats/availability`
  );
  return data;
}

/* ------------------------------ 명령 (Command) ---------------------------- */

export interface CreateEventBody {
  ticketEventName: string;
  ticketOpenAt: string;
  ticketClosedAt: string;
  ticketEventAt: string;
  ticketEventCategory: TicketEventCategory;
}

export interface UpdateEventBody {
  ticketEventName?: string;
  ticketOpenAt?: string;
  ticketClosedAt?: string;
  ticketEventAt?: string;
  ticketEventCategory?: TicketEventCategory;
}

export interface CreateSectionBody {
  sectionName: string;
  grade: string;
  price: number;
  capacity: number;
  seatsPerRow: number;
}

export interface CreateSeatBody {
  sectionId: number;
  rowLabel: string;
  seatNumber: number;
}

export async function createEvent(body: CreateEventBody): Promise<TicketEvent> {
  const { data } = await api.post<TicketEvent>(BASE, body);
  return data;
}

export async function updateEvent(
  id: number,
  body: UpdateEventBody
): Promise<TicketEvent> {
  const { data } = await api.patch<TicketEvent>(`${BASE}/${id}`, body);
  return data;
}

export interface SectionCreationResult {
  ticketEvent: TicketEvent;
  sections: Section[];
}

/** 구역 일괄 생성. 백엔드는 { sections: [...] } 형태를 받는다. */
export async function createSections(
  eventId: number,
  sections: CreateSectionBody[]
): Promise<SectionCreationResult> {
  const { data } = await api.post<SectionCreationResult>(
    `${BASE}/${eventId}/sections`,
    { sections }
  );
  return data;
}

export interface SeatCreationResult {
  ticketEvent: TicketEvent;
  createdSeatCount: number;
}

/** 좌석 자동 생성. 구역 capacity 기준으로 백엔드가 생성한다(요청 본문 없음). */
export async function generateSeats(
  eventId: number
): Promise<SeatCreationResult> {
  const { data } = await api.post<SeatCreationResult>(
    `${BASE}/${eventId}/seats`
  );
  return data;
}

export async function completeSetup(eventId: number): Promise<TicketEvent> {
  const { data } = await api.post<TicketEvent>(`${BASE}/${eventId}/complete`);
  return data;
}

export async function openEvent(eventId: number): Promise<TicketEvent> {
  const { data } = await api.post<TicketEvent>(`${BASE}/${eventId}/open`);
  return data;
}

export async function closeEvent(eventId: number): Promise<TicketEvent> {
  const { data } = await api.post<TicketEvent>(`${BASE}/${eventId}/close`);
  return data;
}

export async function cancelEvent(eventId: number): Promise<TicketEvent> {
  const { data } = await api.post<TicketEvent>(`${BASE}/${eventId}/cancel`);
  return data;
}
