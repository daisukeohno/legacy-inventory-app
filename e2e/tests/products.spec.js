import { expect, test } from '@playwright/test';

test('searches products by keyword and filters low stock', async ({ page }) => {
  await page.goto('/products');
  await expect(page.getByTestId('product-row')).not.toHaveCount(0);

  await page.getByLabel('キーワード').fill('SKU-1001');
  await page.getByRole('button', { name: '検索' }).click();
  await expect(page.getByTestId('product-row')).toHaveCount(1);
  await expect(page.getByText('128,000 円')).toBeVisible();

  await page.getByLabel('キーワード').fill('');
  await page.getByLabel('在庫が少ない商品のみ').check();
  const rows = page.getByTestId('product-row');
  const badges = page.getByTestId('low-stock-badge');
  await expect(badges).not.toHaveCount(0);
  await expect
    .poll(async () => (await rows.count()) - (await badges.count()))
    .toBe(0);
});

test('registers a product and shows it in the list', async ({ page }) => {
  const sku = `SKU-E2E-${Date.now()}`;
  await page.goto('/products/new');
  await page.getByLabel('SKU').fill(sku);
  await page.getByLabel('商品名').fill('E2Eテスト商品');
  await page.getByLabel('価格').fill('12345');
  await page.getByLabel('在庫数').fill('3');
  await page.getByRole('button', { name: '保存' }).click();

  await expect(page).toHaveURL(/\/products$/);
  await page.getByLabel('キーワード').fill(sku);
  await page.getByRole('button', { name: '検索' }).click();
  await expect(page.getByTestId('product-row')).toHaveCount(1);
  await expect(page.getByText('12,345 円')).toBeVisible();
  await expect(page.getByTestId('low-stock-badge')).toBeVisible();
});
