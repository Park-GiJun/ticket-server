# ticket-server-fe

티켓 예매 서버(`ticket-server`)의 프론트엔드. React 19 + TypeScript + Vite SPA.

백엔드 MSA(Eureka + Gateway + user-service + ticket-event-service)의 단일 외부 진입점인
게이트웨이(`http://localhost:18080`)와 통신한다.

## 기술 스택

- React 19 / TypeScript 5.7 / Vite 6
- 라우팅: react-router-dom v7 (`React.lazy` + `Suspense` 코드 스플리팅)
- 서버 상태: @tanstack/react-query v5
- 클라이언트 상태: zustand v5 (+ persist, 인증 토큰 저장)
- HTTP: axios (`/api` baseURL, 요청 시 Bearer 토큰 자동 부착, 401 시 자동 로그아웃)
- 스타일: CSS Modules + 전역 토큰(`src/styles/theme.css`, `global.css`), 폰트는 Pretendard(CDN)

## 실행

```bash
# 의존성 설치
npm install

# 개발 서버 (http://localhost:5173)
npm run dev

# 타입 체크만
npm run typecheck     # = tsc --noEmit

# 프로덕션 빌드 (tsc --noEmit 후 vite build -> dist/)
npm run build

# 빌드 결과 미리보기
npm run preview

# 린트
npm run lint
```

## 백엔드 연동 (dev proxy)

백엔드는 CORS 가 설정되어 있지 않으므로, 개발 시 **Vite dev proxy** 로 동일 출처에서 호출한다.
`vite.config.ts` 의 `server.proxy` 가 `/api` 로 시작하는 요청을 게이트웨이로 전달한다.

```
/api/**  ->  http://localhost:18080  (changeOrigin: true)
```

따라서 프론트는 항상 상대경로 `/api/...` 로만 호출하면 되고, 별도 환경변수가 필요 없다.
백엔드 전체 스택 기동 순서: discovery-server -> gateway -> user-service / ticket-event-service.

인증 흐름:
- 로그인 성공 시 `accessToken` 을 zustand store(`authStore`, localStorage 키 `"ticket-server-auth"`)에 저장.
- axios 요청 인터셉터가 토큰이 있으면 `Authorization: Bearer <token>` 헤더를 부착.
- 응답이 401 이면 인터셉터가 인증 상태를 비우고 `/login` 으로 이동(`/login`, `/signup` 에서는 제외).

## 디렉터리 구조

```
src/
  api/                axios 클라이언트 + 도메인별 API 함수
    client.ts           axios 인스턴스(인터셉터: Bearer 부착 / 401 처리)
    auth.ts             register / login / getMe
    ticketEvents.ts     이벤트·구역·좌석 조회 + 관리용 mutation
  types/              도메인 타입(ticketEvent / auth / common)
  store/authStore.ts  zustand 인증 스토어(persist)
  lib/
    queryClient.ts      react-query QueryClient (staleTime 30s, retry 1)
    format.ts           날짜/가격/카테고리/상태 라벨 포맷터
  components/
    ui/                 디자인 시스템(Button, Input, Card, Badge, Tag,
                        Modal, Spinner, Skeleton, EmptyState) — index.ts 에서 일괄 re-export
    event/EventCard/    이벤트 카드(포스터 placeholder + 상태/카테고리 표시)
    layout/             Header / Footer / Layout / ProtectedRoute
  pages/
    home/               HomePage (+ HeroCarousel / CategoryGrid / EventSection 섹션)
    events/             EventListPage (목록 + 카테고리·상태 필터)
    eventDetail/        EventDetailPage
    booking/            BookingPage (+ SeatMap 좌석 선택)
    auth/               LoginPage / SignupPage (+ validation, useAuthError)
    mypage/             MyPage (ProtectedRoute 보호)
    NotFoundPage.tsx
  router/AppRouter.tsx  BrowserRouter + Routes (lazy 페이지, Layout 하위)
  styles/             theme.css(토큰) / global.css(reset)
  main.tsx            QueryClientProvider + AppRouter 진입점
```

## 라우트

| 경로 | 페이지 | 비고 |
|------|--------|------|
| `/` | HomePage | |
| `/events` | EventListPage | 카테고리/상태 필터 |
| `/events/:eventId` | EventDetailPage | |
| `/events/:eventId/booking` | BookingPage | 좌석 선택 |
| `/login` | LoginPage | |
| `/signup` | SignupPage | |
| `/mypage` | MyPage | `ProtectedRoute` — 미인증 시 `/login` |
| `*` | NotFoundPage | |

모든 라우트는 `Layout`(Header + `<Outlet/>` + Footer) 하위에 렌더된다.
