---
description: 변경 내역을 분석해 Conventional Commits 규칙으로 커밋 생성
argument-hint: "[커밋 메시지 또는 범위 힌트 (선택)]"
allowed-tools: Bash(git status:*), Bash(git diff:*), Bash(git add:*), Bash(git commit:*), Bash(git log:*), Bash(git branch:*), Bash(git restore:*)
---

너는 현재 작업 트리의 변경을 분석해 **하나 이상의 의미 있는 커밋**을 생성한다.

## 입력
- 사용자 인자(선택): `$ARGUMENTS`
  - 메시지/범위 힌트가 주어지면 그에 맞춰 작성한다.
  - 비어 있으면 변경 내역을 보고 스스로 적절한 메시지를 만든다.

## 현재 상태 (자동 수집)
- 브랜치:
!`git branch --show-current`
- 변경 요약:
!`git status --short`
- 스테이지되지 않은 변경:
!`git diff --stat`
- 최근 커밋(스타일 참고):
!`git log --oneline -10`

## 진행 절차
1. **변경 파악**: 위 상태와 필요한 경우 `git diff` 로 무엇이/왜 바뀌었는지 이해한다.
2. **그룹핑 판단**: 성격이 다른 변경(예: 기능 + 문서 + 설정)이 섞였으면,
   논리적으로 분리해 **여러 커밋**으로 나누는 것을 우선 고려한다. 단순/단일 변경이면 한 커밋으로.
3. **스테이징**: 각 커밋에 들어갈 파일만 `git add <paths>` 로 선택적으로 담는다.
   - `.gitignore` 대상이나 빌드 산출물은 담지 않는다.
   - 의도와 무관한 파일이 섞이지 않도록 `git add -A` 남용 금지.
4. **커밋 메시지 작성** — **Conventional Commits** 형식:
   ```
   <type>(<scope>): <제목, 한국어, 명령형 요약>

   <본문: 무엇을/왜 바꿨는지. 필요한 경우만>

   Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>
   ```
   - type: `feat` | `fix` | `docs` | `refactor` | `chore` | `test` | `build` | `style` | `perf`
   - scope 예: `user`, `auth`, `security`, `build`, `docs`, `config`
   - 제목은 50자 내외, 마침표 없이. 본문이 필요하면 한 줄 비우고 작성.
   - **반드시 마지막 줄에 위 `Co-Authored-By` 트레일러를 포함**한다.
5. **커밋 실행**: `git commit` 으로 생성한다. 여러 커밋이면 2~4 단계를 반복한다.

## 금지/주의
- **푸시하지 않는다.** 사용자가 명시적으로 요청할 때만 별도로 푸시한다.
- `--no-verify`, `--amend`, 강제 옵션을 임의로 쓰지 않는다(훅 실패 시 원인을 보고).
- 시크릿/자격증명이 포함된 변경이 보이면 커밋을 멈추고 사용자에게 알린다.
- PowerShell 환경이면 멀티라인 메시지는 here-string(`@' ... '@`) 또는 여러 `-m` 옵션으로 안전하게 전달한다.

## 출력 형식
- 생성한 커밋의 해시·메시지 제목을 나열하고, 무엇을 왜 그렇게 나눴는지 한 줄로 요약한다.
- 스테이징하지 않고 남겨 둔 변경이 있으면 그 이유와 함께 알린다.
