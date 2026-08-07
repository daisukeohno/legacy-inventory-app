import { render, screen, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import ProductTable from './ProductTable.jsx';

const products = [
  { id: 1, sku: 'SKU-1001', name: 'ノートPC 14インチ', price: 128000, stockQuantity: 24, lowStock: false },
  { id: 2, sku: 'SKU-1002', name: 'ワイヤレスマウス', price: 2800, stockQuantity: 6, lowStock: true },
];

function renderTable(items) {
  render(
    <MemoryRouter>
      <ProductTable products={items} />
    </MemoryRouter>,
  );
}

describe('ProductTable', () => {
  it('formats prices with thousand separators and yen', () => {
    renderTable(products);
    expect(screen.getByText('128,000 円')).toBeInTheDocument();
    expect(screen.getByText('2,800 円')).toBeInTheDocument();
  });

  it('shows the low stock badge only for flagged products', () => {
    renderTable(products);
    const rows = screen.getAllByTestId('product-row');
    expect(within(rows[0]).queryByTestId('low-stock-badge')).toBeNull();
    expect(within(rows[1]).getByTestId('low-stock-badge')).toHaveTextContent('在庫少');
  });

  it('renders an empty state', () => {
    renderTable([]);
    expect(screen.getByText('該当する商品がありません。')).toBeInTheDocument();
  });
});
