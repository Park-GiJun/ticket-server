/** 인증 폼 공통 클라이언트 검증 유틸. */

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function validateEmail(email: string): string | undefined {
  const value = email.trim();
  if (!value) return '이메일을 입력해 주세요.';
  if (!EMAIL_RE.test(value)) return '올바른 이메일 형식이 아니에요.';
  return undefined;
}

export function validatePassword(password: string): string | undefined {
  if (!password) return '비밀번호를 입력해 주세요.';
  if (password.length < 8 || password.length > 64)
    return '비밀번호는 8자 이상 64자 이하로 입력해 주세요.';
  return undefined;
}

export function validateName(name: string): string | undefined {
  const value = name.trim();
  if (!value) return '이름을 입력해 주세요.';
  if (value.length < 2) return '이름은 2자 이상 입력해 주세요.';
  return undefined;
}
