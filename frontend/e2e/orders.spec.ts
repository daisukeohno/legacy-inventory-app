import { expect, test } from '@playwright/test'

test.describe('注文', () => {
  test('新規注文で在庫が引き落とされ、注文一覧に合計金額が表示される', async ({ page }) => {
    const suffix = Date.now()
    const sku = `SKU-ORDER-${suffix}`
    const productName = `E2E注文用商品-${suffix}`

    // 在庫20個のテスト商品を登録
    await page.goto('/products/new')
    await page.getByLabel('SKU').fill(sku)
    await page.getByLabel('商品名').fill(productName)
    await page.getByLabel('単価(円)').fill('1500')
    await page.getByLabel('在庫数').fill('20')
    await page.getByRole('button', { name: '保存' }).click()
    await expect(page).toHaveURL(/\/products$/)

    await page.goto('/orders/new')
    await page.getByLabel('得意先名').fill('株式会社E2E商事')
    await page.getByLabel(`${productName} の注文数量`).fill('3')
    await page.getByRole('button', { name: '注文確定' }).click()

    await expect(page).toHaveURL(/\/orders$/)
    const newestOrder = page.locator('tbody tr').first()
    await expect(newestOrder).toContainText('株式会社E2E商事')
    await expect(newestOrder).toContainText(`${productName} × 3 = 4,500 円`)
    await expect(newestOrder.locator('.total-amount')).toHaveText('4,500 円')
    await expect(newestOrder).toContainText('NEW')

    // 在庫が 20 -> 17 に引き落とされている
    await page.goto('/products')
    await page.getByLabel('キーワード:').fill(sku)
    await page.getByRole('button', { name: '検索' }).click()
    await expect(page.locator('tbody tr', { hasText: sku }).getByTestId('stock')).toHaveText('17')
  })

  test('在庫不足の注文はAPIの409エラーメッセージを表示し在庫を変更しない', async ({ page }) => {
    const suffix = Date.now()
    const sku = `SKU-SHORT-${suffix}`
    const productName = `E2E在庫不足商品-${suffix}`

    await page.goto('/products/new')
    await page.getByLabel('SKU').fill(sku)
    await page.getByLabel('商品名').fill(productName)
    await page.getByLabel('単価(円)').fill('800')
    await page.getByLabel('在庫数').fill('2')
    await page.getByRole('button', { name: '保存' }).click()
    await expect(page).toHaveURL(/\/products$/)

    await page.goto('/orders/new')
    await page.getByLabel('得意先名').fill('株式会社在庫不足')
    await page.getByLabel(`${productName} の注文数量`).fill('5')
    await page.getByRole('button', { name: '注文確定' }).click()

    await expect(page.getByRole('alert')).toHaveText(
      `「${productName}」の在庫が不足しています(在庫数: 2)。`,
    )
    await expect(page).toHaveURL(/\/orders\/new$/)

    await page.goto('/products')
    await page.getByLabel('キーワード:').fill(sku)
    await page.getByRole('button', { name: '検索' }).click()
    await expect(page.locator('tbody tr', { hasText: sku }).getByTestId('stock')).toHaveText('2')
  })

  test('得意先名なし・数量0はクライアント側でエラーになる', async ({ page }) => {
    await page.goto('/orders/new')
    await page.getByRole('button', { name: '注文確定' }).click()
    await expect(page.getByRole('alert')).toHaveText('得意先名を入力してください。')

    await page.getByLabel('得意先名').fill('株式会社サンプル商事')
    await page.getByRole('button', { name: '注文確定' }).click()
    await expect(page.getByRole('alert')).toHaveText(
      '少なくとも1つの商品を数量1以上で選択してください。',
    )
  })
})
