import { expect, test } from '@playwright/test';

test.describe('商品一覧', () => {
  test('キーワード検索で商品を絞り込める', async ({ page }) => {
    await page.goto('/products');
    await expect(page.getByTestId('product-row').first()).toBeVisible();

    await page.getByLabel('キーワード(商品名・SKU)').fill('マウス');
    await page.getByRole('button', { name: '検索' }).click();

    await expect(page.getByTestId('product-row')).toHaveCount(1);
    await expect(page.getByTestId('product-row')).toContainText('ワイヤレスマウス');
  });

  test('低在庫フィルタで在庫10未満の商品だけが強調表示される', async ({ page }) => {
    await page.goto('/products');
    await page.getByLabel('低在庫のみ表示').check();

    const rows = page.getByTestId('product-row');
    await expect(rows.filter({ hasText: 'SKU-1002' })).toHaveCount(1);
    await expect(rows.filter({ hasText: 'SKU-1003' })).toHaveCount(1);
    await expect(rows.filter({ hasText: 'SKU-1005' })).toHaveCount(1);
    await expect(rows.filter({ hasText: 'SKU-1001' })).toHaveCount(0);
    for (const row of await rows.all()) {
      await expect(row).toHaveClass(/low-stock/);
      await expect(row.getByText('低在庫')).toBeVisible();
    }
  });

  test('金額は3桁区切り + 円 で表示される', async ({ page }) => {
    await page.goto('/products');
    await expect(page.getByTestId('product-row').first()).toContainText('128,000 円');
  });
});

test.describe('商品登録', () => {
  test('必須項目が空の場合は検証エラーを表示する', async ({ page }) => {
    await page.goto('/products/new');
    await page.getByRole('button', { name: '保存' }).click();

    await expect(page.getByText('SKUを入力してください。')).toBeVisible();
    await expect(page.getByText('商品名を入力してください。')).toBeVisible();
    await expect(page).toHaveURL(/\/products\/new/);
  });

  test('商品を登録すると一覧に表示される', async ({ page }) => {
    const sku = `SKU-E2E-${Date.now()}`;
    await page.goto('/products/new');
    await page.getByLabel('SKU').fill(sku);
    await page.getByLabel('商品名').fill('E2Eテスト商品');
    await page.getByLabel('単価(円)').fill('12345');
    await page.getByLabel('在庫数').fill('4');
    await page.getByRole('button', { name: '保存' }).click();

    await expect(page).toHaveURL(/\/products$/);
    await page.getByLabel('キーワード(商品名・SKU)').fill(sku);
    await page.getByRole('button', { name: '検索' }).click();

    const row = page.getByTestId('product-row');
    await expect(row).toHaveCount(1);
    await expect(row).toContainText('12,345 円');
    await expect(row.getByText('低在庫')).toBeVisible();
  });
});
