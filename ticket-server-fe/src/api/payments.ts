import { api } from './client';
import type { CreatePaymentBody, Payment } from '../types/payment';

const BASE = '/payments';

/* ------------------------------ 명령 (Command) ---------------------------- */

/** 결제 요청(승인). 인증 필요. PG 연동은 백엔드 FakePaymentGateway 가 처리. */
export async function createPayment(body: CreatePaymentBody): Promise<Payment> {
  const { data } = await api.post<Payment>(BASE, body);
  return data;
}

/* ------------------------------ 조회 (Query) ------------------------------ */

export async function getPayment(id: number): Promise<Payment> {
  const { data } = await api.get<Payment>(`${BASE}/${id}`, { silent404: true });
  return data;
}
