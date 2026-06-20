import { test, expect } from '@playwright/test';

// 실행마다 고유 계정(실제 user-service 회원가입을 친다)
const ts = Date.now();
const user = {
  email: `e2e_${ts}@test.com`,
  password: 'e2epass123',
  name: 'E2E테스터',
};

test('홈페이지가 로드되고 헤더가 보인다', async ({ page }) => {
  await page.goto('/');
  await expect(page).toHaveTitle(/TICKET/i);
  const header = page.getByRole('banner');
  await expect(header.getByRole('link', { name: '로그인' })).toBeVisible();
  await expect(header.getByRole('link', { name: '공연/전시' })).toBeVisible();
});

test('공연 목록 페이지에 진입할 수 있다', async ({ page }) => {
  await page.goto('/events');
  // 목록 또는 빈 상태가 떠야 한다(에러로 죽으면 안 됨)
  await expect(page.getByRole('banner')).toBeVisible();
});

test('회원가입 후 로그인까지 동작한다', async ({ page }) => {
  await page.goto('/signup');
  await page.getByLabel('이메일').fill(user.email);
  await page.getByLabel('비밀번호').fill(user.password);
  await page.getByLabel('이름').fill(user.name);
  await page.getByRole('button', { name: '회원가입' }).click();

  // 성공 시 /login 으로 이동
  await expect(page).toHaveURL(/\/login/);

  await page.getByLabel('이메일').fill(user.email);
  await page.getByLabel('비밀번호').fill(user.password);
  await page.getByRole('button', { name: '로그인' }).click();

  // 로그인 성공 → 헤더에 마이페이지가 보여야 함
  await expect(page.getByRole('link', { name: '마이페이지' })).toBeVisible();
});

test('비로그인으로 마이페이지 접근 시 로그인으로 리다이렉트된다', async ({
  page,
}) => {
  await page.goto('/mypage');
  await expect(page).toHaveURL(/\/login/);
});

test('홈 진입 시 콘솔 에러가 없다', async ({ page }) => {
  const errors: string[] = [];
  page.on('console', (m) => {
    if (m.type() === 'error') errors.push(m.text());
  });
  page.on('pageerror', (e) => errors.push(String(e)));
  await page.goto('/');
  await page.waitForTimeout(1500);
  expect(errors, `콘솔 에러:\n${errors.join('\n')}`).toHaveLength(0);
});

test('공연 목록에 노출되는 예매 가능한 공연이 있다(시드 데이터)', async ({
  page,
}) => {
  await page.goto('/events');
  await page.waitForTimeout(1500);
  // 이벤트 카드(공연 항목) 링크가 하나라도 있어야 한다
  const cards = page.locator('a[href^="/events/"]');
  await expect(cards.first()).toBeVisible();
});
