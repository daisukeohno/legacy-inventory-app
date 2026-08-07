import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createOrder, fetchProducts } from '../api.js';
import { formatYen } from '../format.js';

export default function OrderCreatePage() {
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [customerName, setCustomerName] = useState('');
  const [quantities, setQuantities] = useState({});
  const [error, setError] = useState('');

  useEffect(() => {
    fetchProducts()
      .then(setProducts)
      .catch((e) => setError(e.message));
  }, []);

  async function onSubmit(event) {
    event.preventDefault();
    const lines = Object.entries(quantities)
      .map(([productId, quantity]) => ({ productId: Number(productId), quantity: Number(quantity) }))
      .filter((line) => Number.isFinite(line.quantity) && line.quantity > 0);

    if (lines.length === 0) {
      setError('少なくとも1つの商品を数量1以上で選択してください。');
      return;
    }

    try {
      await createOrder({ customerName, lines });
      navigate('/orders');
    } catch (e) {
      setError(e.message);
    }
  }

  return (
    <section>
      <h2>新規注文</h2>
      {error && <p role="alert" className="error">{error}</p>}
      <form className="entity-form" onSubmit={onSubmit}>
        <label>
          得意先名
          <input
            value={customerName}
            aria-label="得意先名"
            onChange={(event) => setCustomerName(event.target.value)}
          />
        </label>
        <table className="data-table">
          <thead>
            <tr>
              <th>SKU</th>
              <th>商品名</th>
              <th className="numeric">価格</th>
              <th className="numeric">在庫数</th>
              <th className="numeric">数量</th>
            </tr>
          </thead>
          <tbody>
            {products.map((product) => (
              <tr key={product.id}>
                <td>{product.sku}</td>
                <td>{product.name}</td>
                <td className="numeric">{formatYen(product.price)}</td>
                <td className="numeric">
                  {product.stockQuantity}
                  {product.lowStock && <span className="badge badge-low-stock">在庫少</span>}
                </td>
                <td className="numeric">
                  <input
                    type="number"
                    min="0"
                    aria-label={`${product.name} の数量`}
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
        <button type="submit">注文を確定する</button>
      </form>
    </section>
  );
}
