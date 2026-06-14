import styles from './Spinner.module.css';

export interface SpinnerProps {
  size?: number;
  className?: string;
  label?: string;
}

export function Spinner({ size = 28, className, label = '로딩 중' }: SpinnerProps) {
  return (
    <span
      className={[styles.wrapper, className ?? ''].filter(Boolean).join(' ')}
      role="status"
      aria-live="polite"
    >
      <span
        className={styles.spinner}
        style={{ width: size, height: size }}
        aria-hidden="true"
      />
      <span className="srOnly">{label}</span>
    </span>
  );
}

export default Spinner;
