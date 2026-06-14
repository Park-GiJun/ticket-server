import { isAxiosError } from 'axios';
import type { ApiError } from '../../types/common';

/**
 * axios 에러(혹은 임의의 throw 값)에서 사용자에게 보여줄 메시지를 추출한다.
 * 백엔드 응답 본문이 ApiError 형태면 그 message 를, 아니면 기본 메시지를 반환.
 */
export function extractErrorMessage(
  err: unknown,
  fallback = '요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요.'
): string {
  if (isAxiosError(err)) {
    const data = err.response?.data as Partial<ApiError> | undefined;
    if (data && typeof data.message === 'string' && data.message.trim()) {
      return data.message;
    }
    if (err.message) {
      return err.message;
    }
  }
  if (err instanceof Error && err.message) {
    return err.message;
  }
  return fallback;
}
