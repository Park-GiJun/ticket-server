import { api } from './client';
import type { CreateReservationBody, Reservation } from '../types/reservation';

const BASE = '/reservations';

/* ------------------------------ 명령 (Command) ---------------------------- */

/** 좌석 점유(hold) + 예약 생성. 인증 필요. */
export async function createReservation(
  body: CreateReservationBody
): Promise<Reservation> {
  const { data } = await api.post<Reservation>(BASE, body);
  return data;
}

/** 예약 취소(점유 해제). */
export async function cancelReservation(id: number): Promise<Reservation> {
  const { data } = await api.post<Reservation>(`${BASE}/${id}/cancel`);
  return data;
}

/* ------------------------------ 조회 (Query) ------------------------------ */

/** 단건 조회. 백엔드 미구현 시 404 → 화면에서 조용히 처리한다. */
export async function getReservation(id: number): Promise<Reservation> {
  const { data } = await api.get<Reservation>(`${BASE}/${id}`, {
    silent404: true,
  });
  return data;
}

/** 내 예매 내역. 백엔드 미구현 시 404 → 빈 상태로 처리한다. */
export async function listMyReservations(): Promise<Reservation[]> {
  const { data } = await api.get<Reservation[]>(`${BASE}/me`, {
    silent404: true,
  });
  return data;
}
