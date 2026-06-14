import type { HTMLAttributes, ReactNode } from 'react';
import styles from './Badge.module.css';

export type BadgeTone =
  | 'primary'
  | 'success'
  | 'danger'
  | 'neutral'
  | 'warning';

export interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  tone?: BadgeTone;
  children?: ReactNode;
}

export function Badge({
  tone = 'neutral',
  className,
  children,
  ...rest
}: BadgeProps) {
  const classes = [styles.badge, styles[tone], className ?? '']
    .filter(Boolean)
    .join(' ');
  return (
    <span className={classes} {...rest}>
      <span className={styles.dot} aria-hidden="true" />
      {children}
    </span>
  );
}

export default Badge;
