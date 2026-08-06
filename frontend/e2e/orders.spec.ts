import { expect, test } from '@playwright/test';

test.describe('注文', () => {
  test('注文一覧に明細と合計金額が表示される', async ({ page }) => {
    await page.goto('/orders');

    const firstOrder = page.getByTestId('order-card').filter({ hasText: '株式会社サンプル商事' });
    await expect(firstOrder).toContainText('株式会社サンプル商事');
    await expect(firstOrder).toContainText('128,000 円');
    await expect(firstOrder.getByTestId('order-total')).toContainText('261,600 円');
  });

  test('新規注文を作成すると在庫が引き落とされる', async ({ page }) => {
    await page.goto('/products');
    const laptopRow = page.getByTestId('product-row').filter({ hasText: 'ノートPC 14インチ' });
    const stockBefore = Number((await laptopRow.locator('td').nth(3).innerText()).replace(/,/g, ''));

    await page.goto('/orders/new');
    await page.getByLabel('得意先名').fill('E2E商事');
    await page.getByLabel('ノートPC 14インチ の注文数量').fill('1');
    await expect(page.getByTestId('estimated-total')).toContainText('128,000 円');
    await page.getByRole('button', { name: '注文を確定する' }).click();

    await expect(page).toHaveURL(/\/orders$/);
    await expect(page.getByTestId('order-card').first()).toContainText('E2E商事');

    await page.goto('/products');
    const stockAfter = Number(
      (await page.getByTestId('product-row').filter({ hasText: 'ノートPC 14インチ' })
        .locator('td').nth(3).innerText()).replace(/,/g, ''),
    );
    expect(stockAfter).toBe(stockBefore - 1);
  });

  test('在庫不足の場合はエラーを表示し注文が作成されない', async ({ page }) => {
    await page.goto('/orders/new');
    await page.getByLabel('得意先名').fill('E2E商事');
    await page.getByLabel('USB-Cハブ (7in1) の注文数量').fill('999');
    await page.getByRole('button', { name: '注文を確定する' }).click();

    await expect(page.getByRole('alert')).toContainText('在庫が不足しています');
    await expect(page).toHaveURL(/\/orders\/new$/);
  });

  test('得意先名や数量が未入力の場合はエラーを表示する', async ({ page }) => {
    await page.goto('/orders/new');
    await page.getByRole('button', { name: '注文を確定する' }).click();
    await expect(page.getByRole('alert')).toContainText('得意先名を入力してください。');

    await page.getByLabel('得意先名').fill('E2E商事');
    await page.getByRole('button', { name: '注文を確定する' }).click();
    await expect(page.getByRole('alert')).toContainText('少なくとも1つの商品を数量1以上で選択してください。');
  });
});
