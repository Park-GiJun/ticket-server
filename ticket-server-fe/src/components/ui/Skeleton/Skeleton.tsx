import type { CSSProperties } from 'react';
import styles from './Skeleton.module.css';

export interface SkeletonProps {
  width?: number | string;
  height?: number | string;
  radius?: number | string;
  className?: string;
}

export function Skeleton({
  width = '100%',
  height = 16,
  radius = 'var(--radius-sm)',
  className,
}: SkeletonProps) {
  const style: CSSProperties = {
    width,
    height,
    borderRadius: typeof radius === 'number' ? `${radius}px` : radius,
  };
  return (
    <span
      className={[styles.skeleton, className ?? ''].filter(Boolean).join(' ')}
      style={style}
      aria-hidden="true"
    />
  );
}

export default Skeleton;
