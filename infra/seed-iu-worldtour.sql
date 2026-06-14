-- 2025 IU 〈H.E.R. WORLD TOUR〉 참고 데모 시드 (ticket DB)
-- 날짜는 now() 기준 상대값이라 실행 시점 기준으로 예매중/오픈예정/마감이 갈린다.
-- 재실행 시 중복을 막기 위해 기존 아이유 데이터를 먼저 비운다(FK 없으므로 좌석→구역→이벤트 순).
SET client_encoding TO 'UTF8';

BEGIN;

DELETE FROM ticket_event_seats
 WHERE ticket_event_id IN (SELECT id FROM ticket_events WHERE ticket_event_name LIKE '2025 IU CONCERT%');
DELETE FROM ticket_event_sections
 WHERE ticket_event_id IN (SELECT id FROM ticket_events WHERE ticket_event_name LIKE '2025 IU CONCERT%');
DELETE FROM ticket_events WHERE ticket_event_name LIKE '2025 IU CONCERT%';

-- 1) 이벤트 10건 (OPEN=예매중 / SCHEDULED=오픈예정 / CLOSED=마감)
INSERT INTO ticket_events
  (ticket_event_name, ticket_open_at, ticket_closed_at, ticket_event_at,
   ticket_event_status, ticket_creation_status, ticket_event_category, created_at, updated_at)
VALUES
  ('2025 IU CONCERT 〈H.E.R. WORLD TOUR〉 - 서울',        now()-interval '20 day', now()+interval '18 day', now()+interval '33 day', 'OPEN',      'COMPLETED', 'CONCERT', now(), now()),
  ('2025 IU CONCERT 〈H.E.R. WORLD TOUR〉 - 인천',        now()-interval '18 day', now()+interval '20 day', now()+interval '37 day', 'OPEN',      'COMPLETED', 'CONCERT', now(), now()),
  ('2025 IU CONCERT 〈H.E.R. WORLD TOUR〉 - 부산',        now()-interval '15 day', now()+interval '25 day', now()+interval '42 day', 'OPEN',      'COMPLETED', 'CONCERT', now(), now()),
  ('2025 IU CONCERT 〈H.E.R. WORLD TOUR〉 - 방콕',        now()-interval '10 day', now()+interval '30 day', now()+interval '48 day', 'OPEN',      'COMPLETED', 'CONCERT', now(), now()),
  ('2025 IU CONCERT 〈H.E.R. WORLD TOUR〉 - 도쿄',        now()+interval '14 day', now()+interval '50 day', now()+interval '62 day', 'SCHEDULED', 'COMPLETED', 'CONCERT', now(), now()),
  ('2025 IU CONCERT 〈H.E.R. WORLD TOUR〉 - 오사카',      now()+interval '16 day', now()+interval '52 day', now()+interval '66 day', 'SCHEDULED', 'COMPLETED', 'CONCERT', now(), now()),
  ('2025 IU CONCERT 〈H.E.R. WORLD TOUR〉 - 타이베이',    now()+interval '20 day', now()+interval '58 day', now()+interval '72 day', 'SCHEDULED', 'COMPLETED', 'CONCERT', now(), now()),
  ('2025 IU CONCERT 〈H.E.R. WORLD TOUR〉 - 싱가포르',    now()+interval '24 day', now()+interval '62 day', now()+interval '78 day', 'SCHEDULED', 'COMPLETED', 'CONCERT', now(), now()),
  ('2025 IU CONCERT 〈H.E.R. WORLD TOUR〉 - 런던',        now()-interval '70 day', now()-interval '20 day', now()-interval '12 day', 'CLOSED',    'COMPLETED', 'CONCERT', now(), now()),
  ('2025 IU CONCERT 〈H.E.R. WORLD TOUR〉 - 로스앤젤레스', now()-interval '65 day', now()-interval '15 day', now()-interval '7 day',  'CLOSED',    'COMPLETED', 'CONCERT', now(), now());

-- 2) 구역 3종/이벤트 (VIP/R/S) — 이벤트 이름으로 연결
INSERT INTO ticket_event_sections
  (ticket_event_id, section_name, grade, price, capacity, created_at, updated_at)
SELECT e.id, v.section_name, v.grade, v.price, v.capacity, now(), now()
FROM ticket_events e
CROSS JOIN (VALUES
  ('VIP석', 'VIP', 198000::bigint, 30),
  ('R석',   'R',   154000::bigint, 60),
  ('S석',   'S',   132000::bigint, 90)
) AS v(section_name, grade, price, capacity)
WHERE e.ticket_event_name LIKE '2025 IU CONCERT%';

-- 3) 좌석 — 구역 capacity 만큼 생성. 10석/행(A,B,C...), 일부 SOLD/HELD 로 잔여석 데모.
INSERT INTO ticket_event_seats
  (section_id, ticket_event_id, row_label, seat_number, status, created_at, updated_at)
SELECT
  s.id,
  s.ticket_event_id,
  chr(65 + ((gs.n - 1) / 10)),
  ((gs.n - 1) % 10) + 1,
  CASE WHEN gs.n % 7 = 0 THEN 'SOLD'
       WHEN gs.n % 13 = 0 THEN 'HELD'
       ELSE 'AVAILABLE' END,
  now(), now()
FROM ticket_event_sections s
CROSS JOIN LATERAL generate_series(1, s.capacity) AS gs(n)
WHERE s.ticket_event_id IN (SELECT id FROM ticket_events WHERE ticket_event_name LIKE '2025 IU CONCERT%');

COMMIT;
