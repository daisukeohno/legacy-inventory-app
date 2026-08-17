import { expect, test } from '@playwright/test'

test.describe.configure({ mode: 'serial' })

const uniqueSuffix = () => Date.now().toString().slice(-8)

test('商品一覧をキーワード検索できる', async ({ page }) => {
  await page.goto('/products')
  await expect(page.getByTestId('product-row')).not.toHaveCount(0)

  await page.getByLabel('キーワード').fill('マウス')
  await page.getByRole('button', { name: '検索' }).click()

  await expect(page.getByTestId('product-row')).toHaveCount(1)
  await expect(page.getByTestId('product-row')).toHaveAttribute('data-sku', 'SKU-1002')
})

test('低在庫フィルタは在庫10未満の商品だけを表示する', async ({ page }) => {
  await page.goto('/products')
  await page.getByLabel('低在庫のみ表示').check()

  // 在庫十分な商品が消えるまで待ってから、残った行を検証する。
  await expect(page.locator('[data-sku="SKU-1001"]')).toHaveCount(0)
  const rows = page.getByTestId('product-row')
  await expect(rows).not.toHaveCount(0)
  await expect(page.getByTestId('low-stock-badge')).toHaveCount(await rows.count())
})

test('商品を登録でき、入力不備はエラー表示になる', async ({ page }) => {
  const sku = `SKU-E2E-${uniqueSuffix()}`

  await page.goto('/products/new')
  await page.getByRole('button', { name: '保存' }).click()
  await expect(page.getByText('SKUを入力してください。')).toBeVisible()
  await expect(page.getByText('商品名を入力してください。')).toBeVisible()

  await page.getByLabel('SKU').fill(sku)
  await page.getByLabel('商品名').fill('E2Eテスト商品')
  await page.getByLabel('価格').fill('-1')
  await page.getByLabel('在庫数').fill('5')
  await page.getByRole('button', { name: '保存' }).click()
  await expect(page.getByText('価格は0以上で入力してください。')).toBeVisible()

  await page.getByLabel('価格').fill('1200')
  await page.getByRole('button', { name: '保存' }).click()

  await expect(page).toHaveURL(/\/products$/)
  await page.getByLabel('キーワード').fill(sku)
  await page.getByRole('button', { name: '検索' }).click()
  const row = page.locator(`[data-sku="${sku}"]`)
  await expect(row).toHaveCount(1)
  await expect(row).toContainText('1,200 円')
  await expect(row.getByTestId('low-stock-badge')).toBeVisible()
})

test('注文一覧は明細と合計金額を表示する', async ({ page }) => {
  await page.goto('/orders')
  await expect(page.getByTestId('order-card')).not.toHaveCount(0)
  // シード注文: ノートPC 128,000 x2 + ワイヤレスマウス 2,800 x2 = 261,600
  // 他テストが作る注文の影響を受けないよう、シード注文をテキストで特定する。
  const seeded = page
    .getByTestId('order-card')
    .filter({ hasText: '株式会社サンプル商事' })
    .first()
  await expect(seeded).toContainText('ノートPC 14インチ')
  await expect(seeded.getByTestId('order-total')).toHaveText('261,600 円')
})

test('新規注文を確定すると在庫が引き落とされる', async ({ page }) => {
  await page.goto('/products')
  await page.getByLabel('キーワード').fill('SKU-1004')
  await page.getByRole('button', { name: '検索' }).click()
  const stockCell = page.locator('[data-sku="SKU-1004"] td').nth(3)
  const before = Number((await stockCell.innerText()).replace(/[^0-9]/g, ''))

  await page.goto('/orders/new')
  await page.getByLabel('得意先名').fill('E2E得意先')
  await page.getByLabel('外付けSSD 1TB の数量').fill('2')
  await expect(page.getByTestId('client-total')).toHaveText('31,600 円')
  await page.getByRole('button', { name: '注文を確定' }).click()

  await expect(page).toHaveURL(/\/orders$/)
  const newest = page.getByTestId('order-card').filter({ hasText: 'E2E得意先' }).first()
  await expect(newest.getByTestId('order-total')).toHaveText('31,600 円')

  await page.goto('/products')
  await page.getByLabel('キーワード').fill('SKU-1004')
  await page.getByRole('button', { name: '検索' }).click()
  await expect(stockCell).toContainText(String(before - 2))
})

test('在庫不足の注文はエラーになり在庫は変わらない', async ({ page }) => {
  await page.goto('/orders/new')
  await page.getByLabel('得意先名').fill('E2E在庫不足')
  await page.getByLabel('USB-Cハブ (7in1) の数量').fill('999')
  await page.getByRole('button', { name: '注文を確定' }).click()

  await expect(page.getByTestId('order-error')).toBeVisible()
  await expect(page).toHaveURL(/\/orders\/new$/)

  await page.goto('/products')
  await page.getByLabel('キーワード').fill('SKU-1003')
  await page.getByRole('button', { name: '検索' }).click()
  await expect(page.locator('[data-sku="SKU-1003"] td').nth(3)).toContainText('3')
})
