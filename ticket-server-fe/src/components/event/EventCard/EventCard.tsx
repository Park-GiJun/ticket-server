import { Link } from 'react-router-dom';
import type { TicketEvent, TicketEventStatus } from '../../../types/ticketEvent';
import type { BadgeTone } from '../../ui';
import { Badge } from '../../ui';
import { categoryLabel, formatDate, statusLabel } from '../../../lib/format';
import styles from './EventCard.module.css';

export interface EventCardProps {
  event: TicketEvent;
}

const STATUS_TONE: Record<TicketEventStatus, BadgeTone> = {
  SCHEDULED: 'warning',
  OPEN: 'primary',
  CLOSED: 'neutral',
  SOLD_OUT: 'danger',
  CANCELLED: 'danger',
  COMPLETED: 'neutral',
};

export function EventCard({ event }: EventCardProps) {
  return (
    <Link
      to={`/events/${event.id}`}
      className={styles.card}
      aria-label={`${event.ticketEventName} 상세보기`}
    >
      <div
        className={styles.poster}
        data-category={event.ticketEventCategory}
      >
        <span className={styles.posterLabel}>
          {categoryLabel(event.ticketEventCategory)}
        </span>
      </div>
      <div className={styles.body}>
        <div className={styles.meta}>
          <span className={styles.categoryTag}>
            {categoryLabel(event.ticketEventCategory)}
          </span>
          <Badge tone={STATUS_TONE[event.ticketEventStatus]}>
            {statusLabel(event.ticketEventStatus)}
          </Badge>
        </div>
        <h3 className={styles.title}>{event.ticketEventName}</h3>
        <p className={styles.date}>{formatDate(event.ticketEventAt)}</p>
      </div>
    </Link>
  );
}

export default EventCard;
