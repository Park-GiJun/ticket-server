import type { ButtonHTMLAttributes, ReactNode } from 'react';
import styles from './Tag.module.css';

export interface TagProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  active?: boolean;
  children?: ReactNode;
}

export function Tag({
  active = false,
  className,
  children,
  type,
  ...rest
}: TagProps) {
  const classes = [styles.tag, active ? styles.active : '', className ?? '']
    .filter(Boolean)
    .join(' ');
  return (
    <button
      type={type ?? 'button'}
      className={classes}
      aria-pressed={active}
      {...rest}
    >
      {children}
    </button>
  );
}

export default Tag;
