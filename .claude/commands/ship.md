---
description: 커밋→Jira(KAN)→Notion→Slack→GitHub 푸시를 한 번에 동기화하는 ticket-server 일정관리 파이프라인
argument-hint: "[작업 요약 또는 범위 힌트 (선택)] [#KAN-키 또는 epic=reservation|payment (선택)]"
allowed-tools: Bash(git status:*), Bash(git diff:*), Bash(git add:*), Bash(git commit:*), Bash(git log:*), Bash(git branch:*), Bash(git push:*), Bash(git remote:*), Bash(git rev-parse:*), mcp__claude_ai_Atlassian__createJiraIssue, mcp__claude_ai_Atlassian__searchJiraIssuesUsingJql, mcp__claude_ai_Atlassian__getTransitionsForJiraIssue, mcp__claude_ai_Atlassian__transitionJiraIssue, mcp__claude_ai_Atlassian__addCommentToJiraIssue, mcp__claude_ai_Atlassian__editJiraIssue, mcp__notion__notion-create-pages, mcp__slack__slack_post_message
---

너는 ticket-server 의 **단일 일정관리 파이프라인**이다. 작업 트리의 변경을 커밋하고,
**Jira(정본) → Notion(로그) → Slack(알림) → GitHub(푸시)** 네 서비스를 한 번에 동기화한다.

## 고정 리소스 (이 프로젝트 전용 — 변경 시 이 파일 수정)

| 키 | 값 |
|----|----|
| Jira cloudId | `c0caf068-84da-4325-8063-45edd0daa2b7` (gijun.atlassian.net) |
| Jira projectKey | `KAN` ("내 칸반 스페이스") · 이슈타입: `작업` / `에픽` / `하위 작업` |
| Jira 전이 ID | 해야 할 일=`11` · 진행 중=`21` · 완료=`31` (모든 이슈 공통) |
| Notion data_source_id | `e4bd1bad-07ad-4084-8eae-34fd9071d20f` (커밋 로그 DB) |
| Notion 허브 page_id | `38581e31-b648-81d0-b791-f0c4d9fabfd2` |
| Slack channel_id | `C0BBY1G754J` (`schedule-history` = 일정 히스토리) — /ship 동기화 알림 전용 |

## Slack 채널 맵 (용도별 분리 — 봇 참여 확인됨 2026-06-20)

| 채널 | channel_id | 용도 |
|------|-----------|------|
| `schedule-history` | `C0BBY1G754J` | **/ship 커밋·배포 동기화 알림** (이 스킬이 쓰는 채널) |
| `issue-report` | `C0BC1KUJVNG` | AI 테스트 실행 후 발견 이슈 자동 업로드 |
| `application-error-be` | `C0BBVUFGRGE` | 백엔드 런타임 오류 모니터링 (추후) |
| `application-error-fe` | `C0BBRL7T9FD` | 프론트엔드 오류 모니터링 (추후) |

> 구 `새-채널`(`C0BBG7271AT`)은 더 이상 쓰지 않는다.

## 도메인 에픽 맵 (정본 — 작업 이슈는 변경된 모듈에 맞는 에픽 하위로 묶는다)

| 에픽 | 도메인 | 모듈/라벨 |
|------|--------|-----------|
| `KAN-1` | 플랫폼·인프라 | gateway · common · discovery-server · infra · config · build |
| `KAN-2` | 인증 | user-service · gateway(JWT) |
| `KAN-3` | 티켓 이벤트 | ticket-event |
| `KAN-4` | 예매 (진행 중) | reservation |
| `KAN-5` | 결제 (신규) | payment |
| `KAN-29` | 프론트엔드 | frontend (ticket-server-fe) |
| `KAN-30` | 배포·CI/CD | infra · build · config (Docker/TeamCity/배포) |
| `KAN-31` | 오류 모니터링·관측성 | observability (BE/FE 오류·로그·메트릭·추적) |
| `KAN-32` | AI 테스트·QA | qa · test (AI 러너·issue-report·E2E·부하) |

> KAN-27/KAN-28 은 중복이라 폐기(완료)했다. 절대 쓰지 말 것.

## 입력
- `$ARGUMENTS` — 작업 요약/범위 힌트(선택). `#KAN-12` 형태가 있으면 **기존 이슈**를 재사용하고,
  `epic=KAN-4` 처럼 명시하면 그 에픽 하위로 새 이슈를 만든다. 없으면 위 맵에서 모듈→에픽을 자동 선택한다.

## 현재 상태 (자동 수집)
- 브랜치:
!`git branch --show-current`
- 변경 요약:
!`git status --short`
- 변경 통계:
!`git diff --stat`
- 최근 커밋(스타일 참고):
!`git log --oneline -8`
- 원격 존재 여부:
!`git remote -v`

## 진행 절차

### 0. 사전 점검
- 변경이 없으면 멈추고 알린다. 시크릿/자격증명이 보이면 멈추고 보고한다.
- 성격이 다른 변경(기능+문서+설정)이 섞였으면 **여러 묶음**으로 나눠 각 묶음마다 1~5 를 반복한다.

### 1. Jira 이슈 확보 (정본 먼저)
- `$ARGUMENTS` 에 `#KAN-키` 가 있으면 그 이슈를 사용. 없으면 변경 내용으로 **작업(Task)** 이슈를 새로 만든다.
  - `createJiraIssue`: `cloudId`, `projectKey=KAN`, `issueTypeName=작업`, `summary`(한국어 명령형),
    `description`(무엇을/왜), `additional_fields.labels=[<모듈>, <커밋타입>]` (예: `["reservation-service","feat"]`).
  - **도메인 에픽 맵**에서 변경 모듈에 맞는 에픽 키를 골라 `parent` 에 넣어 에픽 하위로 묶는다(예: reservation→`KAN-4`).
  - 모듈 라벨은 위 맵의 라벨(`reservation` `payment` `ticket-event` `user-service` `gateway` `common` `discovery-server` `infra`)과 커밋타입을 함께 단다.
- 반환된 **이슈 키(KAN-xx)** 와 webUrl 을 기억한다.

### 2. 커밋 (GitHub 으로 연결)
- 해당 묶음 파일만 선택적으로 `git add <paths>` (빌드 산출물·gitignore 대상 제외, `git add -A` 남용 금지).
- **Conventional Commits** + 본문에 Jira 키:
  ```
  <type>(<scope>): <제목, 한국어, 명령형, ~50자, 마침표 없음>

  <본문: 필요 시 무엇을/왜>

  Refs: KAN-xx
  Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>
  ```
  - type: `feat|fix|docs|refactor|chore|test|build|style|perf` · scope 예: `reservation` `payment` `gateway` `auth` `config` `docs`
  - PowerShell 멀티라인은 here-string(`@' ... '@`) 또는 여러 `-m` 옵션으로 전달.
- `--no-verify`/`--amend`/강제 옵션 금지. 훅 실패 시 원인 보고 후 중단.
- 커밋 후 `git rev-parse --short HEAD` 로 **단축 해시**를 얻는다.

### 3. GitHub 푸시
- 원격이 있으면 현재 브랜치를 `git push`(필요 시 `-u origin <branch>`). 원격이 없으면 **푸시는 건너뛰고** 그 사실을 출력에 명시한다.
- 푸시 성공 시 커밋 URL을 만들 수 있으면 기억한다(원격 origin 기준). 없으면 빈 값.

### 4. Notion 커밋 로그 기록
- `notion-create-pages` 로 `parent.data_source_id = e4bd1bad-07ad-4084-8eae-34fd9071d20f` 아래 1행 생성:
  - `제목` = 커밋 제목, `커밋타입` = type, `모듈` = 모듈 라벨(들),
    `상태` = `완료`(푸시까지 끝났으면) / `진행 중`, `Jira키` = `KAN-xx`,
    `커밋해시` = 단축 해시, `GitHub` = 커밋 URL(있으면), `날짜` = 오늘(ISO, `date:날짜:start`).

### 5. Jira 상태 전이 + Slack 알림
- `transitionJiraIssue` 로 이슈를 **완료(`31`)** 로 옮긴다(작업이 미완이면 진행 중 `21`). 전이 ID는 상단 표 참고.
  필요하면 `addCommentToJiraIssue` 로 커밋 해시를 코멘트로 남긴다.
- `slack_post_message`(`channel_id=C0BBY1G754J`, `schedule-history`)로 알림 전송:
  ```
  :rocket: <type>(<scope>): <제목>
  • Jira: <KAN-xx webUrl>
  • 커밋: <단축 해시>  • 모듈: <모듈>
  • GitHub: <커밋 URL 또는 "원격 미설정">
  ```

## 출력 형식
한 줄 표로 동기화 결과를 요약한다 — **커밋 해시·제목 / Jira 키(+URL) / Notion 기록 / Slack 전송 / GitHub 푸시** 각각 ✅/⏭️(건너뜀)/❌(실패+이유).
여러 묶음이면 묶음별로 나열한다. 스테이징하지 않고 남긴 변경이 있으면 이유와 함께 알린다.

## 실패 처리 (부분 성공 허용)
- 4단계 이후 어느 서비스가 실패해도 **커밋/푸시는 이미 끝났으므로 롤백하지 않는다.** 실패한 서비스만 표시하고,
  사용자가 재시도할 수 있도록 필요한 ID(KAN 키·해시)를 출력에 남긴다.
- Slack 전송이 `not_in_channel` 로 실패하면 "`schedule-history` 에 봇 초대 필요"를 안내한다.
