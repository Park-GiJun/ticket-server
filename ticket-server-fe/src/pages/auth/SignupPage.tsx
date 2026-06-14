import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Button, Card, Input } from '../../components/ui';
import { registerApi } from '../../api/auth';
import { extractErrorMessage } from './useAuthError';
import { validateEmail, validateName, validatePassword } from './validation';
import styles from './AuthForm.module.css';

export default function SignupPage() {
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [errors, setErrors] = useState<{
    email?: string;
    password?: string;
    name?: string;
  }>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  function validate(): boolean {
    const next = {
      email: validateEmail(email),
      password: validatePassword(password),
      name: validateName(name),
    };
    setErrors(next);
    return !next.email && !next.password && !next.name;
  }

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setFormError(null);
    if (!validate()) return;

    setSubmitting(true);
    try {
      await registerApi({
        email: email.trim(),
        password,
        name: name.trim(),
      });
      navigate('/login', {
        replace: true,
        state: { signupCompleted: true },
      });
    } catch (err) {
      setFormError(
        extractErrorMessage(err, '회원가입에 실패했어요. 입력 정보를 확인해 주세요.')
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className={styles.page}>
      <Card className={styles.card} padding="lg" as="section">
        <div className={styles.header}>
          <Link to="/" className={styles.brand}>
            TICKET
          </Link>
          <h1 className={styles.title}>회원가입</h1>
          <p className={styles.subtitle}>
            몇 가지 정보만 입력하면 바로 시작할 수 있어요.
          </p>
        </div>

        <form className={styles.form} onSubmit={handleSubmit} noValidate>
          {formError && (
            <div className={styles.formError} role="alert">
              <span className={styles.formErrorIcon} aria-hidden="true">
                ⚠
              </span>
              <span>{formError}</span>
            </div>
          )}

          <Input
            label="이메일"
            type="email"
            name="email"
            autoComplete="email"
            placeholder="you@example.com"
            value={email}
            error={errors.email}
            disabled={submitting}
            onChange={(e) => {
              setEmail(e.target.value);
              if (errors.email)
                setErrors((p) => ({ ...p, email: undefined }));
            }}
          />

          <Input
            label="비밀번호"
            type="password"
            name="password"
            autoComplete="new-password"
            placeholder="8자 이상 64자 이하"
            hint="영문, 숫자를 조합하면 더 안전해요."
            value={password}
            error={errors.password}
            disabled={submitting}
            onChange={(e) => {
              setPassword(e.target.value);
              if (errors.password)
                setErrors((p) => ({ ...p, password: undefined }));
            }}
          />

          <Input
            label="이름"
            type="text"
            name="name"
            autoComplete="name"
            placeholder="홍길동"
            value={name}
            error={errors.name}
            disabled={submitting}
            onChange={(e) => {
              setName(e.target.value);
              if (errors.name)
                setErrors((p) => ({ ...p, name: undefined }));
            }}
          />

          <Button
            type="submit"
            variant="primary"
            size="lg"
            fullWidth
            loading={submitting}
            className={styles.submit}
          >
            회원가입
          </Button>
        </form>

        <p className={styles.footer}>
          이미 계정이 있으신가요?
          <Link to="/login" className={styles.link}>
            로그인
          </Link>
        </p>
      </Card>
    </div>
  );
}
