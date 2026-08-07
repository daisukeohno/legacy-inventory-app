import { expect, test } from '@playwright/test';

async function createProduct(request, apiBaseURL, stockQuantity) {
  const sku = `SKU-E2E-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
  const response = await request.post(`${apiBaseURL}/api/products`, {
    data: { sku, name: `E2E商品 ${sku}`, price: 1000, stockQuantity },
  });
  expect(response.status()).toBe(201);
  return response.json();
}

test('places an order and decreases stock', async ({ page, request }) => {
  const apiBaseURL = test.info().project.use.apiBaseURL ?? 'http://localhost:8080';
  const product = await createProduct(request, apiBaseURL, 20);

  await page.goto('/orders/new');
  await page.getByLabel('得意先名').fill('株式会社E2E商事');
  await page.getByLabel(`${product.name} の数量`).fill('3');
  await page.getByRole('button', { name: '注文を確定する' }).click();

  await expect(page).toHaveURL(/\/orders$/);
  await expect(page.getByTestId('order-card').first()).toContainText('株式会社E2E商事');
  await expect(page.getByTestId('order-card').first()).toContainText('3,000 円');

  const after = await (await request.get(`${apiBaseURL}/api/products/${product.id}`)).json();
  expect(after.stockQuantity).toBe(17);
});

test('shows an error and creates no order when stock is insufficient', async ({ page, request }) => {
  const apiBaseURL = test.info().project.use.apiBaseURL ?? 'http://localhost:8080';
  const product = await createProduct(request, apiBaseURL, 2);
  const ordersBefore = await (await request.get(`${apiBaseURL}/api/orders`)).json();

  await page.goto('/orders/new');
  await page.getByLabel('得意先名').fill('株式会社E2E商事');
  await page.getByLabel(`${product.name} の数量`).fill('5');
  await page.getByRole('button', { name: '注文を確定する' }).click();

  await expect(page.getByRole('alert')).toContainText('在庫が不足しています');
  await expect(page).toHaveURL(/\/orders\/new$/);

  const after = await (await request.get(`${apiBaseURL}/api/products/${product.id}`)).json();
  expect(after.stockQuantity).toBe(2);
  const ordersAfter = await (await request.get(`${apiBaseURL}/api/orders`)).json();
  expect(ordersAfter.length).toBe(ordersBefore.length);
});
