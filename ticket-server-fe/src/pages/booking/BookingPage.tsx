import { useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
  getSeatAvailability,
  getSeats,
  getSections,
} from '../../api/ticketEvents';
import type { Seat, Section } from '../../types/ticketEvent';
import {
  Badge,
  Button,
  Card,
  EmptyState,
  Modal,
  Skeleton,
} from '../../components/ui';
import { formatPrice } from '../../lib/format';
import SeatMap from './SeatMap';
import styles from './BookingPage.module.css';

/** 1인 최대 예매 매수 */
const MAX_SEATS = 4;

export default function BookingPage() {
  const { eventId: eventIdParam } = useParams<{ eventId: string }>();
  const eventId = Number(eventIdParam);
  const validId = Number.isFinite(eventId) && eventId > 0;

  const [selectedSectionId, setSelectedSectionId] = useState<number | null>(
    null
  );
  const [selectedSeatIds, setSelectedSeatIds] = useState<ReadonlySet<number>>(
    () => new Set()
  );
  const [payModalOpen, setPayModalOpen] = useState(false);

  const sectionsQuery = useQuery({
    queryKey: ['sections', eventId],
    queryFn: () => getSections(eventId),
    enabled: validId,
  });

  const seatsQuery = useQuery({
    queryKey: ['seats', eventId],
    queryFn: () => getSeats(eventId),
    enabled: validId,
  });

  const availabilityQuery = useQuery({
    queryKey: ['seat-availability', eventId],
    queryFn: () => getSeatAvailability(eventId),
    enabled: validId,
  });

  const sections = sectionsQuery.data ?? [];
  const allSeats = seatsQuery.data ?? [];

  /** 구역별 잔여 좌석 수(AVAILABLE) 집계 */
  const remainingBySection = useMemo<Map<number, number>>(() => {
    const map = new Map<number, number>();
    for (const seat of allSeats) {
      if (seat.status !== 'AVAILABLE') continue;
      map.set(seat.sectionId, (map.get(seat.sectionId) ?? 0) + 1);
    }
    return map;
  }, [allSeats]);

  const selectedSection = useMemo<Section | null>(
    () => sections.find((s) => s.id === selectedSectionId) ?? null,
    [sections, selectedSectionId]
  );

  /** 선택된 구역의 좌석만 */
  const sectionSeats = useMemo<Seat[]>(() => {
    if (selectedSectionId == null) return [];
    return allSeats.filter((seat) => seat.sectionId === selectedSectionId);
  }, [allSeats, selectedSectionId]);

  /** 선택된 좌석 객체 목록(요약·합계용) */
  const selectedSeats = useMemo<Seat[]>(
    () => allSeats.filter((seat) => selectedSeatIds.has(seat.id)),
    [allSeats, selectedSeatIds]
  );

  const totalPrice = useMemo<number>(() => {
    if (!selectedSection) return 0;
    return selectedSection.price * selectedSeats.length;
  }, [selectedSection, selectedSeats.length]);

  const canSelectMore = selectedSeatIds.size < MAX_SEATS;

  /** 구역 변경 시 좌석 선택 초기화 */
  const handleSelectSection = (section: Section) => {
    if (section.id === selectedSectionId) return;
    setSelectedSectionId(section.id);
    setSelectedSeatIds(new Set());
  };

  const handleToggleSeat = (seat: Seat) => {
    setSelectedSeatIds((prev) => {
      const next = new Set(prev);
      if (next.has(seat.id)) {
        next.delete(seat.id);
      } else {
        if (next.size >= MAX_SEATS) return prev;
        next.add(seat.id);
      }
      return next;
    });
  };

  const handleRemoveSeat = (seatId: number) => {
    setSelectedSeatIds((prev) => {
      const next = new Set(prev);
      next.delete(seatId);
      return next;
    });
  };

  /** 좌석 라벨 정렬(요약 표시용) */
  const sortedSelectedSeats = useMemo<Seat[]>(
    () =>
      [...selectedSeats].sort((a, b) =>
        a.rowLabel === b.rowLabel
          ? a.seatNumber - b.seatNumber
          : a.rowLabel.localeCompare(b.rowLabel, 'en')
      ),
    [selectedSeats]
  );

  /* ----------------------------- 가드 / 로딩 ----------------------------- */

  if (!validId) {
    return (
      <div className={`container ${styles.page}`}>
        <EmptyState
          icon="⚠️"
          title="잘못된 접근이에요"
          description="유효하지 않은 공연입니다."
          action={
            <Link to="/events">
              <Button variant="secondary">공연 목록으로</Button>
            </Link>
          }
        />
      </div>
    );
  }

  const isLoading =
    sectionsQuery.isLoading || seatsQuery.isLoading || availabilityQuery.isLoading;
  const isError = sectionsQuery.isError || seatsQuery.isError;

  if (isError) {
    return (
      <div className={`container ${styles.page}`}>
        <EmptyState
          icon="⚠️"
          title="예매 정보를 불러오지 못했어요"
          description="잠시 후 다시 시도해 주세요."
          action={
            <Button
              variant="secondary"
              onClick={() => {
                sectionsQuery.refetch();
                seatsQuery.refetch();
                availabilityQuery.refetch();
              }}
            >
              다시 시도
            </Button>
          }
        />
      </div>
    );
  }

  /* -------------------------------- 렌더 -------------------------------- */

  return (
    <div className={`container ${styles.page}`}>
      <header className={styles.header}>
        <div>
          <p className={styles.eyebrow}>좌석 예매</p>
          <h1 className={styles.heading}>좌석을 선택해 주세요</h1>
        </div>
        <Link to={`/events/${eventId}`} className={styles.backLink}>
          ← 공연 정보로
        </Link>
      </header>

      {isLoading ? (
        <div className={styles.layout}>
          <div className={styles.main}>
            <Card>
              <Skeleton width="40%" height={22} />
              <div className={styles.skeletonSections}>
                {Array.from({ length: 3 }).map((_, i) => (
                  <Skeleton key={i} height={64} radius="var(--radius-md)" />
                ))}
              </div>
            </Card>
          </div>
          <aside className={styles.aside}>
            <Card>
              <Skeleton width="50%" height={20} />
              <div className={styles.skeletonSummary}>
                <Skeleton height={48} radius="var(--radius-md)" />
                <Skeleton height={48} radius="var(--radius-md)" />
              </div>
            </Card>
          </aside>
        </div>
      ) : sections.length === 0 ? (
        <EmptyState
          icon="🪑"
          title="아직 좌석이 준비되지 않았어요"
          description="이 공연은 예매 가능한 구역이 없습니다."
          action={
            <Link to={`/events/${eventId}`}>
              <Button variant="secondary">공연 정보로</Button>
            </Link>
          }
        />
      ) : (
        <div className={styles.layout}>
          <div className={styles.main}>
            {/* (1) 구역 선택 */}
            <Card>
              <h2 className={styles.sectionTitle}>구역 선택</h2>
              <ul className={styles.sectionList}>
                {sections.map((section) => {
                  const remaining = remainingBySection.get(section.id) ?? 0;
                  const soldOut = remaining === 0;
                  const active = section.id === selectedSectionId;
                  return (
                    <li key={section.id}>
                      <button
                        type="button"
                        className={[
                          styles.sectionItem,
                          active ? styles.sectionActive : '',
                          soldOut ? styles.sectionSoldOut : '',
                        ]
                          .filter(Boolean)
                          .join(' ')}
                        onClick={() => handleSelectSection(section)}
                        disabled={soldOut}
                        aria-pressed={active}
                      >
                        <span className={styles.sectionInfo}>
                          <span className={styles.sectionName}>
                            {section.sectionName}
                            <span className={styles.sectionGrade}>
                              {section.grade}
                            </span>
                          </span>
                          <span className={styles.sectionPrice}>
                            {formatPrice(section.price)}
                          </span>
                        </span>
                        <span className={styles.sectionMeta}>
                          {soldOut ? (
                            <Badge tone="danger">매진</Badge>
                          ) : (
                            <Badge tone="primary">
                              잔여 {remaining.toLocaleString('ko-KR')}석
                            </Badge>
                          )}
                        </span>
                      </button>
                    </li>
                  );
                })}
              </ul>
            </Card>

            {/* (2) 좌석맵 */}
            <Card>
              <div className={styles.seatMapHeader}>
                <h2 className={styles.sectionTitle}>좌석 선택</h2>
                {selectedSection && (
                  <span className={styles.seatMapHint}>
                    {selectedSection.sectionName} · 최대 {MAX_SEATS}석
                  </span>
                )}
              </div>

              {!selectedSection ? (
                <EmptyState
                  icon="👆"
                  title="구역을 먼저 선택해 주세요"
                  description="구역을 선택하면 좌석 배치도가 나타나요."
                />
              ) : sectionSeats.length === 0 ? (
                <EmptyState
                  icon="🪑"
                  title="좌석 정보가 없어요"
                  description="이 구역에는 등록된 좌석이 없습니다."
                />
              ) : (
                <SeatMap
                  seats={sectionSeats}
                  selectedIds={selectedSeatIds}
                  onToggle={handleToggleSeat}
                  canSelectMore={canSelectMore}
                />
              )}
            </Card>
          </div>

          {/* (3) 선택 요약 */}
          <aside className={styles.aside}>
            <Card className={styles.summaryCard}>
              <h2 className={styles.sectionTitle}>선택 내역</h2>

              {sortedSelectedSeats.length === 0 ? (
                <p className={styles.summaryEmpty}>
                  선택한 좌석이 없습니다.
                </p>
              ) : (
                <ul className={styles.seatChips}>
                  {sortedSelectedSeats.map((seat) => (
                    <li key={seat.id} className={styles.seatChip}>
                      <span>
                        {seat.rowLabel}
                        {seat.seatNumber}
                      </span>
                      <button
                        type="button"
                        className={styles.seatChipRemove}
                        aria-label={`${seat.rowLabel}${seat.seatNumber} 선택 해제`}
                        onClick={() => handleRemoveSeat(seat.id)}
                      >
                        ×
                      </button>
                    </li>
                  ))}
                </ul>
              )}

              <dl className={styles.summaryRows}>
                <div className={styles.summaryRow}>
                  <dt>구역</dt>
                  <dd>{selectedSection?.sectionName ?? '-'}</dd>
                </div>
                <div className={styles.summaryRow}>
                  <dt>매수</dt>
                  <dd>{sortedSelectedSeats.length}매</dd>
                </div>
                <div className={`${styles.summaryRow} ${styles.summaryTotal}`}>
                  <dt>합계</dt>
                  <dd>{formatPrice(totalPrice)}</dd>
                </div>
              </dl>

              <Button
                fullWidth
                size="lg"
                disabled={sortedSelectedSeats.length === 0}
                onClick={() => setPayModalOpen(true)}
              >
                결제하기
              </Button>
            </Card>
          </aside>
        </div>
      )}

      {/* 모바일 하단 고정 요약바 */}
      {!isLoading && sections.length > 0 && (
        <div className={styles.mobileBar} role="region" aria-label="선택 요약">
          <div className={styles.mobileBarInfo}>
            <span className={styles.mobileBarCount}>
              {selectedSeats.length}매 선택
            </span>
            <strong className={styles.mobileBarPrice}>
              {formatPrice(totalPrice)}
            </strong>
          </div>
          <Button
            size="lg"
            disabled={selectedSeats.length === 0}
            onClick={() => setPayModalOpen(true)}
          >
            결제하기
          </Button>
        </div>
      )}

      <Modal
        open={payModalOpen}
        onClose={() => setPayModalOpen(false)}
        title="예매 기능 준비 중"
      >
        <div className={styles.modalBody}>
          <div className={styles.modalIcon} aria-hidden="true">
            🛠️
          </div>
          <p className={styles.modalText}>
            예매 기능은 현재 준비 중입니다.
            <br />
            (reservation 도메인 추가 예정)
          </p>
          {sortedSelectedSeats.length > 0 && selectedSection && (
            <div className={styles.modalSummary}>
              <span>
                {selectedSection.sectionName} ·{' '}
                {sortedSelectedSeats.length}매
              </span>
              <strong>{formatPrice(totalPrice)}</strong>
            </div>
          )}
          <Button fullWidth onClick={() => setPayModalOpen(false)}>
            확인
          </Button>
        </div>
      </Modal>
    </div>
  );
}
