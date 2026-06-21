import type { ReactNode } from 'react';
import { Button } from '../Button/Button';
import { EmptyState } from '../EmptyState/EmptyState';

export interface ErrorStateProps {
  icon?: ReactNode;
  title?: string;
  description?: string;
  /** 제공 시 "다시 시도" 버튼을 렌더한다. */
  onRetry?: () => void;
  retryLabel?: string;
  /** onRetry 대신 커스텀 액션을 넣고 싶을 때. */
  action?: ReactNode;
  className?: string;
}

/**
 * 데이터 로딩 실패 등 인라인 에러 상태 공통 컴포넌트.
 * 각 페이지에 흩어져 있던 "불러오지 못했어요 + 다시 시도" 블록을 일원화한다.
 */
export function ErrorState({
  icon = '⚠️',
  title = '문제가 발생했어요',
  description = '잠시 후 다시 시도해 주세요.',
  onRetry,
  retryLabel = '다시 시도',
  action,
  className,
}: ErrorStateProps) {
  const resolvedAction =
    action ??
    (onRetry ? (
      <Button variant="secondary" onClick={onRetry}>
        {retryLabel}
      </Button>
    ) : undefined);

  return (
    <EmptyState
      icon={icon}
      title={title}
      description={description}
      action={resolvedAction}
      className={className}
    />
  );
}

export default ErrorState;
