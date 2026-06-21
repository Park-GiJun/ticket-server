import { Component, type ErrorInfo, type ReactNode } from 'react';
import ErrorPage from '../../pages/ErrorPage';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
}

/**
 * 렌더링 중 발생한 예외를 잡아 전체 화면 에러 폴백을 보여주는 안전망.
 * 앱 최상단(main.tsx)에서 라우터를 감싼다.
 */
export class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false };

  static getDerivedStateFromError(): State {
    return { hasError: true };
  }

  componentDidCatch(error: Error, info: ErrorInfo): void {
    // 추후 관측성(KAN-31, application-error-fe) 연동 지점
    console.error('[ErrorBoundary]', error, info.componentStack);
  }

  private handleReset = (): void => {
    this.setState({ hasError: false });
  };

  render(): ReactNode {
    if (this.state.hasError) {
      return <ErrorPage onReset={this.handleReset} />;
    }
    return this.props.children;
  }
}

export default ErrorBoundary;
