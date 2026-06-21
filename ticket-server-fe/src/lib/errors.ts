import axios from 'axios';

/** 네트워크(서버 연결 불가) 시 공통 메시지 */
export const NETWORK_ERROR_MESSAGE =
  '서버에 연결할 수 없어요. 잠시 후 다시 시도해 주세요.';

/** 미구현 API(404) 호출 시 공통 메시지 */
export const NOT_IMPLEMENTED_MESSAGE =
  '아직 개발되지 않은 기능입니다. (개발 예정)';

/**
 * 임의의 에러에서 사람이 읽을 메시지를 추출하는 단일 진입점.
 * axios 에러는 응답 본문의 `message` → 상태별 기본 메시지 순으로 해석한다.
 */
export function getErrorMessage(
  error: unknown,
  fallback = '요청 처리 중 오류가 발생했어요.'
): string {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status;
    if (status === undefined) return NETWORK_ERROR_MESSAGE;
    if (status === 404) return NOT_IMPLEMENTED_MESSAGE;
    const data = error.response?.data as { message?: string } | undefined;
    if (data?.message) return data.message;
  }
  if (error instanceof Error && error.message) return error.message;
  return fallback;
}
