import { expect, test } from '@playwright/test';

const API = process.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

async function stockOf(request: import('@playwright/test').APIRequestContext, sku: string) {
  const response = await request.get(`${API}/api/products?keyword=${encodeURIComponent(sku)}`);
  const products = await response.json();
  return products[0].stockQuantity as number;
}

test('商品一覧の検索と低在庫フィルタ', async ({ page }) => {
  await page.goto('/products');
  await expect(page.getByTestId('product-row')).not.toHaveCount(0);

  await page.getByLabel('キーワード').fill('マウス');
  await page.getByRole('button', { name: '検索' }).click();
  await expect(page.getByTestId('product-row')).toHaveCount(1);
  await expect(page.getByTestId('product-row')).toContainText('ワイヤレスマウス');

  await page.getByLabel('キーワード').fill('');
  await page.getByLabel('在庫少のみ表示').check();
  await page.getByRole('button', { name: '検索' }).click();
  const rows = page.getByTestId('product-row');
  await expect(rows).not.toHaveCount(0);
  for (const row of await rows.all()) {
    await expect(row).toHaveClass(/low-stock/);
    await expect(row.locator('.badge')).toHaveText('在庫少');
  }
});

test('商品登録', async ({ page }) => {
  const sku = `SKU-E2E-${Date.now()}`;
  await page.goto('/products/new');
  await page.getByLabel('SKU').fill(sku);
  await page.getByLabel('商品名').fill('E2Eテスト商品');
  await page.getByLabel('価格 (円)').fill('1234');
  await page.getByLabel('在庫数').fill('50');
  await page.getByRole('button', { name: '保存' }).click();

  await page.waitForURL('**/products');
  await page.getByLabel('キーワード').fill(sku);
  await page.getByRole('button', { name: '検索' }).click();
  await expect(page.getByTestId('product-row')).toContainText('1,234 円');
});

test('正常注文が作成される', async ({ page }) => {
  await page.goto('/orders/new');
  await page.getByLabel('得意先名').fill('株式会社サンプル商事');
  await page.getByLabel('外付けSSD 1TB の数量').fill('2');
  await expect(page.getByTestId('order-total')).toHaveText('合計: 31,600 円');
  await page.getByRole('button', { name: '注文を確定' }).click();

  await page.waitForURL('**/orders');
  await expect(page.getByTestId('order-card').first()).toContainText('株式会社サンプル商事');
});

test('在庫不足時は注文全体が失敗し在庫が変わらない', async ({ page, request }) => {
  const beforeOk = await stockOf(request, 'SKU-1001');
  const beforeNg = await stockOf(request, 'SKU-1003');

  await page.goto('/orders/new');
  await page.getByLabel('得意先名').fill('在庫不足テスト');
  await page.getByLabel('ノートPC 14インチ の数量').fill('1');
  await page.getByLabel('USB-Cハブ (7in1) の数量').fill('999');
  await page.getByRole('button', { name: '注文を確定' }).click();

  await expect(page.getByTestId('order-error')).toContainText('在庫が不足しています');
  expect(await stockOf(request, 'SKU-1001')).toBe(beforeOk);
  expect(await stockOf(request, 'SKU-1003')).toBe(beforeNg);
});
