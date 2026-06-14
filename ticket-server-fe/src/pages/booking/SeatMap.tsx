import { useMemo } from 'react';
import type { Seat, SeatStatus } from '../../types/ticketEvent';
import { seatStatusLabel } from '../../lib/format';
import styles from './SeatMap.module.css';

export interface SeatMapProps {
  /** 선택된 구역의 좌석 목록 */
  seats: Seat[];
  /** 현재 선택된 좌석 id 집합 */
  selectedIds: ReadonlySet<number>;
  /** 좌석 클릭(선택/해제 토글) — 선택 가능한 좌석만 호출됨 */
  onToggle: (seat: Seat) => void;
  /** 추가 선택이 가능한지(최대 매수 미만인지) */
  canSelectMore: boolean;
}

/** rowLabel 정렬: 자연스러운 행 순서(A,B,...,AA) 유지 */
function compareRow(a: string, b: string): number {
  if (a.length !== b.length) return a.length - b.length;
  return a.localeCompare(b, 'en');
}

const LEGEND: SeatStatus[] = ['AVAILABLE', 'HELD', 'SOLD', 'BLOCKED'];

export function SeatMap({
  seats,
  selectedIds,
  onToggle,
  canSelectMore,
}: SeatMapProps) {
  /** rowLabel -> seatNumber 정렬된 2차원 구조 */
  const rows = useMemo(() => {
    const byRow = new Map<string, Seat[]>();
    for (const seat of seats) {
      const list = byRow.get(seat.rowLabel);
      if (list) list.push(seat);
      else byRow.set(seat.rowLabel, [seat]);
    }
    return [...byRow.entries()]
      .sort(([a], [b]) => compareRow(a, b))
      .map(([rowLabel, rowSeats]) => ({
        rowLabel,
        seats: [...rowSeats].sort((a, b) => a.seatNumber - b.seatNumber),
      }));
  }, [seats]);

  return (
    <div className={styles.wrapper}>
      <div className={styles.stageArea}>
        <div className={styles.stage}>STAGE</div>
      </div>

      <div
        className={styles.scrollArea}
        role="group"
        aria-label="좌석 선택"
      >
        <div className={styles.grid}>
          {rows.map(({ rowLabel, seats: rowSeats }) => (
            <div key={rowLabel} className={styles.row}>
              <span className={styles.rowLabel} aria-hidden="true">
                {rowLabel}
              </span>
              <div className={styles.rowSeats}>
                {rowSeats.map((seat) => {
                  const isSelected = selectedIds.has(seat.id);
                  const isAvailable = seat.status === 'AVAILABLE';
                  const disabled =
                    !isAvailable || (!isSelected && !canSelectMore);
                  const stateClass = isSelected
                    ? styles.selected
                    : styles[seat.status.toLowerCase()];
                  return (
                    <button
                      key={seat.id}
                      type="button"
                      className={[styles.seat, stateClass]
                        .filter(Boolean)
                        .join(' ')}
                      disabled={disabled}
                      aria-pressed={isSelected}
                      aria-label={`${rowLabel}열 ${seat.seatNumber}번 좌석 · ${
                        isSelected ? '선택됨' : seatStatusLabel(seat.status)
                      }`}
                      title={`${rowLabel}-${seat.seatNumber} (${seatStatusLabel(
                        seat.status
                      )})`}
                      onClick={() => {
                        if (isAvailable) onToggle(seat);
                      }}
                    >
                      {seat.seatNumber}
                    </button>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      </div>

      <ul className={styles.legend} aria-label="좌석 상태 안내">
        <li className={styles.legendItem}>
          <span className={[styles.legendSwatch, styles.selected].join(' ')} />
          선택
        </li>
        {LEGEND.map((status) => (
          <li key={status} className={styles.legendItem}>
            <span
              className={[
                styles.legendSwatch,
                styles[status.toLowerCase()],
              ].join(' ')}
            />
            {seatStatusLabel(status)}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default SeatMap;
