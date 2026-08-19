import { expect, test } from '@playwright/test'

test.describe('商品(在庫)一覧', () => {
  test('キーワード検索と低在庫フィルタが動作する', async ({ page }) => {
    await page.goto('/products')
    await expect(page.getByRole('heading', { name: '商品(在庫)一覧' })).toBeVisible()

    // シードデータが表示され、金額は3桁区切り + 円
    await expect(page.locator('tbody tr', { hasText: 'SKU-1001' })).toHaveCount(1)
    await expect(page.getByRole('cell', { name: '128,000 円' })).toBeVisible()

    // キーワード検索(商品名部分一致)
    await page.getByLabel('キーワード:').fill('マウス')
    await page.getByRole('button', { name: '検索' }).click()
    await expect(page.getByRole('cell', { name: 'ワイヤレスマウス' })).toBeVisible()
    await expect(page.locator('tbody tr', { hasText: 'SKU-1001' })).toHaveCount(0)

    // SKU部分一致でも検索できる
    await page.getByLabel('キーワード:').fill('SKU-1003')
    await page.getByRole('button', { name: '検索' }).click()
    await expect(page.getByRole('cell', { name: 'USB-Cハブ (7in1)' })).toBeVisible()

    // 低在庫フィルタ: 在庫10未満のみ(シードでは SKU-1002/1003/1005)
    await page.getByLabel('キーワード:').fill('')
    await page.getByRole('checkbox', { name: '在庫少のみ' }).check()
    const rows = page.locator('tbody tr')
    await expect(page.locator('tbody tr', { hasText: 'SKU-1002' })).toHaveCount(1)
    await expect(rows.filter({ hasText: 'SKU-1003' })).toHaveCount(1)
    await expect(rows.filter({ hasText: 'SKU-1005' })).toHaveCount(1)
    // 在庫十分な商品は除外され、表示中の全行が強調されている
    await expect(rows.filter({ hasText: 'SKU-1001' })).toHaveCount(0)
    expect(await rows.count()).toBe(await page.locator('tbody tr.low-stock-row').count())
    await expect(page.locator('.low-stock-badge').first()).toHaveText('在庫少')
  })

  test('商品を登録すると一覧に反映される', async ({ page }) => {
    const sku = `SKU-E2E-${Date.now()}`

    await page.goto('/products')
    await page.getByRole('link', { name: '＋新規商品登録' }).click()

    // クライアント側バリデーション
    await page.getByRole('button', { name: '保存' }).click()
    await expect(page.getByRole('alert')).toContainText('SKUを入力してください。')

    await page.getByLabel('SKU').fill(sku)
    await page.getByLabel('商品名').fill('E2Eテスト商品')
    await page.getByLabel('単価(円)').fill('3500')
    await page.getByLabel('在庫数').fill('4')
    await page.getByRole('button', { name: '保存' }).click()

    await expect(page).toHaveURL(/\/products$/)
    await page.getByLabel('キーワード:').fill(sku)
    await page.getByRole('button', { name: '検索' }).click()

    const row = page.locator('tbody tr', { hasText: sku })
    await expect(row).toHaveClass(/low-stock-row/)
    await expect(row.getByText('3,500 円')).toBeVisible()
    await expect(row.locator('.low-stock-badge')).toHaveText('在庫少')
  })
})
