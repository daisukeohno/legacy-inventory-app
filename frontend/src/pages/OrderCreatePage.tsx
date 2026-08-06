import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createOrder, fetchProducts } from '../api';
import { formatQuantity, formatYen } from '../format';
import type { Product } from '../types';

export function OrderCreatePage() {
  const navigate = useNavigate();
  const [products, setProducts] = useState<Product[]>([]);
  const [customerName, setCustomerName] = useState('');
  const [quantities, setQuantities] = useState<Record<number, string>>({});
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    fetchProducts('', false)
      .then(setProducts)
      .catch((e: unknown) => setError(e instanceof Error ? e.message : '商品一覧の取得に失敗しました。'));
  }, []);

  const estimatedTotal = useMemo(
    () =>
      products.reduce((total, product) => {
        const quantity = Number(quantities[product.id] ?? '');
        if (!Number.isFinite(quantity) || quantity <= 0) {
          return total;
        }
        return total + product.price * quantity;
      }, 0),
    [products, quantities],
  );

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setError(null);

    if (!customerName.trim()) {
      setError('得意先名を入力してください。');
      return;
    }

    const items = products
      .map((product) => ({ productId: product.id, quantity: Number(quantities[product.id] ?? '') }))
      .filter((item) => Number.isInteger(item.quantity) && item.quantity > 0);

    if (items.length === 0) {
      setError('少なくとも1つの商品を数量1以上で選択してください。');
      return;
    }

    setSaving(true);
    try {
      await createOrder({ customerName: customerName.trim(), items });
      navigate('/orders');
    } catch (e) {
      setError(e instanceof Error ? e.message : '注文の登録に失敗しました。');
    } finally {
      setSaving(false);
    }
  }

  return (
    <section>
      <h2>新規注文</h2>
      {error && <p className="error" role="alert">{error}</p>}
      <form onSubmit={handleSubmit}>
        <div className="card">
          <div className="field">
            <label htmlFor="customerName">得意先名</label>
            <input
              id="customerName"
              type="text"
              value={customerName}
              onChange={(event) => setCustomerName(event.target.value)}
            />
          </div>
        </div>

        <div className="card">
          <table>
            <thead>
              <tr>
                <th>SKU</th>
                <th>商品名</th>
                <th className="numeric">単価</th>
                <th className="numeric">在庫数</th>
                <th className="numeric">注文数量</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id} className={product.lowStock ? 'low-stock' : undefined}>
                  <td>{product.sku}</td>
                  <td>
                    {product.name} {product.lowStock && <span className="badge">低在庫</span>}
                  </td>
                  <td className="numeric">{formatYen(product.price)}</td>
                  <td className="numeric">{formatQuantity(product.stockQuantity)}</td>
                  <td className="numeric">
                    <input
                      type="number"
                      min={0}
                      aria-label={`${product.name} の注文数量`}
                      value={quantities[product.id] ?? ''}
                      onChange={(event) =>
                        setQuantities({ ...quantities, [product.id]: event.target.value })
                      }
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <p className="total" data-testid="estimated-total">
            注文予定金額: {formatYen(estimatedTotal)}
          </p>
        </div>

        <div className="actions">
          <button type="submit" disabled={saving}>注文を確定する</button>
          <button type="button" className="secondary" onClick={() => navigate('/orders')}>
            キャンセル
          </button>
        </div>
      </form>
    </section>
  );
}
