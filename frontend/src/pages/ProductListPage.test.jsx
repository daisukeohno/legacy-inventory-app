import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import ProductListPage from './ProductListPage.jsx';

const all = [
  { id: 1, sku: 'SKU-1001', name: 'ノートPC 14インチ', price: 128000, stockQuantity: 24, lowStock: false },
  { id: 2, sku: 'SKU-1002', name: 'ワイヤレスマウス', price: 2800, stockQuantity: 6, lowStock: true },
];

beforeEach(() => {
  global.fetch = vi.fn(async (url) => ({
    ok: true,
    status: 200,
    json: async () => (String(url).includes('lowStock=true') ? [all[1]] : all),
  }));
});

afterEach(() => {
  vi.restoreAllMocks();
});

describe('ProductListPage', () => {
  it('lists products and filters by low stock', async () => {
    render(
      <MemoryRouter>
        <ProductListPage />
      </MemoryRouter>,
    );

    await waitFor(() => expect(screen.getAllByTestId('product-row')).toHaveLength(2));

    await userEvent.click(screen.getByLabelText('在庫が少ない商品のみ'));

    await waitFor(() => expect(screen.getAllByTestId('product-row')).toHaveLength(1));
    expect(screen.getByTestId('low-stock-badge')).toBeInTheDocument();
  });
});
