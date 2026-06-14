import { api } from './client';
import type { LoginBody, LoginResponse, RegisterBody, User } from '../types/auth';

export async function registerApi(body: RegisterBody): Promise<User> {
  const { data } = await api.post<User>('/auth/register', body);
  return data;
}

export async function loginApi(body: LoginBody): Promise<LoginResponse> {
  const { data } = await api.post<LoginResponse>('/auth/login', body);
  return data;
}

export async function getMeApi(): Promise<User> {
  const { data } = await api.get<User>('/users/me');
  return data;
}
