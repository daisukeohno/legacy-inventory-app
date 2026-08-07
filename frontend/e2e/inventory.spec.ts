import { expect, test } from '@playwright/test'

const uniqueSku = () => `SKU-E2E-${Date.now()}`

test('商品を検索し、低在庫が視覚強調される', async ({ page }) => {
  await page.goto('/products')

  await page.getByPlaceholder('商品名 / SKU').fill('SKU-1003')
  await page.getByRole('button', { name: '検索' }).click()

  const row = page.locator('tbody tr').first()
  await expect(row).toContainText('USB-Cハブ')
  await expect(row).toContainText('4,500 円')
  await expect(row.locator('.badge')).toHaveText('在庫少')
  await expect(row).toHaveClass(/low-stock-row/)
})

test('商品を登録し、一覧に反映される', async ({ page }) => {
  const sku = uniqueSku()

  await page.goto('/products/new')
  await page.getByLabel('SKU').fill(sku)
  await page.getByLabel('商品名').fill('E2Eテスト商品')
  await page.getByLabel('単価(円)').fill('1500')
  await page.getByLabel('在庫数').fill('50')
  await page.getByRole('button', { name: '保存' }).click()

  await expect(page).toHaveURL(/\/products$/)
  await page.getByPlaceholder('商品名 / SKU').fill(sku)
  await page.getByRole('button', { name: '検索' }).click()
  await expect(page.locator('tbody tr').first()).toContainText('1,500 円')
})

test('新規注文の正常系: 注文一覧に合計金額が表示される', async ({ page }) => {
  await page.goto('/orders/new')

  await page.getByLabel('得意先').selectOption({ index: 1 })
  await page.getByLabel('ノートPC 14インチ の注文数量').fill('1')
  await page.getByRole('button', { name: '注文確定' }).click()

  await expect(page).toHaveURL(/\/orders$/)
  await expect(page.locator('tbody tr').first()).toContainText('128,000 円')
})

test('在庫不足の異常系: エラーメッセージが表示され注文されない', async ({ page }) => {
  await page.goto('/orders/new')

  await page.getByLabel('得意先').selectOption({ index: 1 })
  await page.getByLabel('USB-Cハブ (7in1) の注文数量').fill('9999')
  await page.getByRole('button', { name: '注文確定' }).click()

  await expect(page.getByRole('alert')).toContainText('在庫が不足しています')
  await expect(page).toHaveURL(/\/orders\/new$/)
})
