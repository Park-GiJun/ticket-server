---
description: 단위/서비스/e2e 중 유형과 타겟을 골라 테스트 코드 작성
argument-hint: "[unit|service|e2e] [타겟 클래스/경로]  (생략 시 물어봄)"
allowed-tools: Glob, Grep, Read, Edit, Write, AskUserQuestion, Bash(./gradlew*), Bash(git status:*)
---

너는 이 프로젝트(Kotlin + Spring Boot, **Kotest + MockK + Testcontainers**)의 테스트 코드를 작성한다.

## 입력 해석
- 인자: `$ARGUMENTS` — `[유형] [타겟]` 형태.
  - **유형**(`unit` | `service` | `e2e`)과 **타겟**(클래스명·파일경로·도메인)이 명확하면 그대로 진행.
  - 둘 중 하나라도 빠지면 `AskUserQuestion` 으로 묻는다:
    - 질문1 "테스트 유형": `단위(unit)` / `서비스(service)` / `e2e` 중 택1.
    - 질문2 "타겟": 자유 입력(어떤 클래스/유스케이스/엔드포인트를 테스트할지).

## 테스트 유형별 방침
- **unit (단위)**: 외부 의존 없는 순수 로직. 도메인 모델(불변식·상태 전이)·매퍼·핸들러 단위.
  - 협력자는 `MockK`(`mockk()`, `every {} returns`, `verify {}`)로 대체. Spring 컨텍스트 **미기동**.
  - 도메인 모델은 예외 케이스(`shouldThrow<...>`)와 정상 전이를 함께 검증.
- **service (서비스)**: 애플리케이션/슬라이스 통합. 핸들러 + 포트, 또는 `@WebMvcTest`/`@DataJpaTest` 같은 슬라이스.
  - out 포트는 MockK 로, 웹 슬라이스는 `MockMvc` 로. 가능한 한 가벼운 슬라이스를 고른다.
- **e2e**: 전체 컨텍스트 + 실제 인프라. `@SpringBootTest(webEnvironment = RANDOM_PORT)` +
  **Testcontainers**(`postgresql`, 필요 시 `kafka`). 실제 HTTP 호출(`TestRestTemplate`/`WebTestClient`)로 시나리오 검증.

## 진행 절차
1. **컨벤션 파악**: `src/test/**` 에 기존 테스트가 있으면 Glob/Read 로 스타일(Kotest 스펙 종류: `FunSpec`/`BehaviorSpec` 등, 네이밍, 패키지)을 따른다. 없으면 `FunSpec` 기본.
2. **타겟 분석**: 대상 소스를 Read 해 public API·분기·예외·상태 전이를 식별하고 테스트 케이스 목록을 만든다.
3. **작성**: 타겟과 동일 패키지의 `src/test/kotlin/...` 경로에 `<Target>Test.kt` 또는 `<Target>{유형}Test.kt` 로 Write.
   - Kotest 어서션(`shouldBe`, `shouldThrow`, `shouldContain`)과 MockK 를 사용한다.
   - 경계값·실패 경로·동시성 관련 분기를 빠뜨리지 않는다.
4. **검증**: 해당 모듈만 컴파일/테스트 실행 — `./gradlew :<module>:test --tests "<FQCN>"`.
   - Windows 에서 `JAVA_HOME` 미설정이면, 실행 전 IntelliJ JBR 경로를 사용하도록 사용자에게 안내하거나 `-Dorg.gradle.java.home` 을 활용한다.
   - 실패하면 원인을 분석해 테스트 또는 (명백한 버그면) 보고 후 수정한다.

## 출력 형식 (한국어)
- 생성한 테스트 파일 경로, 커버한 케이스 목록, 실행 결과(통과/실패)를 요약한다.
- 타겟에서 테스트하기 어려운 부분(설계상 결합 등)을 발견하면 개선점으로 함께 지적한다.

## 주의
- 프로덕션 코드는 명백한 버그 수정이 아니면 건드리지 않는다.
- 단정적 가짜 통과(assertion 없는 테스트)를 만들지 않는다. 모든 테스트는 의미 있는 단언을 포함한다.
