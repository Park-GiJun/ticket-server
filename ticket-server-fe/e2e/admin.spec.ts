import { test, expect } from '@playwright/test';

const admin = { email: 'admin@ticket.com', password: 'admin1234' };

test('관리자 로그인 후 헤더에 관리자 메뉴가 보인다', async ({ page }) => {
  await page.goto('/login');
  await page.getByLabel('이메일').fill(admin.email);
  await page.getByLabel('비밀번호').fill(admin.password);
  await page.getByRole('button', { name: '로그인' }).click();

  await expect(
    page.getByRole('banner').getByRole('link', { name: '관리자' })
  ).toBeVisible();
});

test('비관리자는 /admin 접근 시 홈으로 막힌다', async ({ page }) => {
  // 비로그인 상태로 /admin → 로그인으로
  await page.goto('/admin');
  await expect(page).toHaveURL(/\/login/);
});

test('관리자가 공연을 생성하고 구역·좌석·오픈까지 진행한다 (시드 생성)', async ({
  page,
}) => {
  // 로그인
  await page.goto('/login');
  await page.getByLabel('이메일').fill(admin.email);
  await page.getByLabel('비밀번호').fill(admin.password);
  await page.getByRole('button', { name: '로그인' }).click();
  await expect(
    page.getByRole('banner').getByRole('link', { name: '관리자' })
  ).toBeVisible();

  // 공연 생성
  const name = `E2E 데모공연 ${Date.now()}`;
  await page.goto('/admin/events/new');
  await page.getByPlaceholder('예: IU 월드투어 in 서울').fill(name);
  const dts = page.locator('input[type="datetime-local"]');
  await dts.nth(0).fill('2026-07-01T10:00');
  await dts.nth(1).fill('2026-07-20T23:59');
  await dts.nth(2).fill('2026-08-01T19:00');
  await page.getByRole('button', { name: '공연 생성' }).click();

  // 관리 페이지 이동
  await expect(page).toHaveURL(/\/admin\/events\/\d+/);
  await expect(page.getByRole('heading', { name })).toBeVisible();

  // 구역 추가
  await page.getByPlaceholder('구역명 (예: A구역)').fill('A구역');
  const nums = page.locator('input[type="number"]');
  await nums.nth(0).fill('110000'); // 가격
  await nums.nth(1).fill('50'); // 좌석수
  await page.getByRole('button', { name: '저장' }).click();
  await expect(page.getByText('A구역')).toBeVisible();

  // 좌석 생성 → 셋업 완료 → 오픈
  await page.getByRole('button', { name: '좌석 생성' }).click();
  await page.waitForTimeout(1500);
  await page.getByRole('button', { name: '셋업 완료' }).click();
  await page.waitForTimeout(1500);
  await page.getByRole('button', { name: '예매 오픈' }).click();
  await page.waitForTimeout(1500);

  // 공연 목록(로그인 상태)에 노출되는지 = 시드가 실제로 생성됨
  await page.goto('/events');
  await expect(page.getByText(name).first()).toBeVisible();
});
