import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, type Product } from '../api';
import { formatYen } from '../format';

export default function OrderFormPage() {
  const navigate = useNavigate();
  const [products, setProducts] = useState<Product[]>([]);
  const [customerName, setCustomerName] = useState('');
  const [quantities, setQuantities] = useState<Record<number, string>>({});
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api
      .listProducts('', false)
      .then(setProducts)
      .catch((e: Error) => setError(e.message));
  }, []);

  const items = useMemo(
    () =>
      products
        .map((p) => ({ product: p, quantity: Number(quantities[p.id] ?? 0) }))
        .filter((line) => Number.isFinite(line.quantity) && line.quantity > 0),
    [products, quantities],
  );

  const total = items.reduce((sum, line) => sum + line.product.price * line.quantity, 0);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    if (items.length === 0) {
      setError('少なくとも1つの商品を数量1以上で選択してください。');
      return;
    }
    try {
      await api.createOrder({
        customerName,
        items: items.map((line) => ({ productId: line.product.id, quantity: line.quantity })),
      });
      navigate('/orders');
    } catch (e) {
      setError((e as Error).message);
    }
  };

  return (
    <section>
      <h2>新規注文</h2>
      {error && (
        <p className="error" data-testid="order-error">
          {error}
        </p>
      )}
      <form className="stack" onSubmit={submit}>
        <label>
          得意先名
          <input value={customerName} onChange={(e) => setCustomerName(e.target.value)} required />
        </label>
        <table>
          <thead>
            <tr>
              <th>SKU</th>
              <th>商品名</th>
              <th className="num">単価</th>
              <th className="num">在庫数</th>
              <th className="num">数量</th>
            </tr>
          </thead>
          <tbody>
            {products.map((p) => (
              <tr key={p.id} className={p.lowStock ? 'low-stock' : undefined}>
                <td>{p.sku}</td>
                <td>{p.name}</td>
                <td className="num">{formatYen(p.price)}</td>
                <td className="num">
                  {p.stockQuantity}
                  {p.lowStock && <span className="badge">在庫少</span>}
                </td>
                <td className="num">
                  <input
                    type="number"
                    min="0"
                    step="1"
                    aria-label={`${p.name} の数量`}
                    value={quantities[p.id] ?? ''}
                    onChange={(e) => setQuantities({ ...quantities, [p.id]: e.target.value })}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <p className="total" data-testid="order-total">
          合計: {formatYen(total)}
        </p>
        <div>
          <button type="submit">注文を確定</button>
        </div>
      </form>
    </section>
  );
}
