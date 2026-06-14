import type { ElementType, HTMLAttributes, ReactNode } from 'react';
import styles from './Card.module.css';

export type CardPadding = 'none' | 'sm' | 'md' | 'lg';

export interface CardProps extends HTMLAttributes<HTMLElement> {
  padding?: CardPadding;
  as?: ElementType;
  hoverable?: boolean;
  children?: ReactNode;
}

export function Card({
  padding = 'md',
  as,
  hoverable = false,
  className,
  children,
  ...rest
}: CardProps) {
  const Component = (as ?? 'div') as ElementType;
  const classes = [
    styles.card,
    styles[`pad-${padding}`],
    hoverable ? styles.hoverable : '',
    className ?? '',
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <Component className={classes} {...rest}>
      {children}
    </Component>
  );
}

export default Card;
