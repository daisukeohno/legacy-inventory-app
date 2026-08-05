import { useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { api, type Product } from '../api';

export default function ProductFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const existing = useLocation().state as Product | null;

  const [sku, setSku] = useState(existing?.sku ?? '');
  const [name, setName] = useState(existing?.name ?? '');
  const [price, setPrice] = useState(String(existing?.price ?? ''));
  const [stockQuantity, setStockQuantity] = useState(String(existing?.stockQuantity ?? ''));
  const [error, setError] = useState<string | null>(null);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    const body = {
      sku,
      name,
      price: Number(price),
      stockQuantity: Number(stockQuantity),
    };
    try {
      if (id) {
        await api.updateProduct(Number(id), body);
      } else {
        await api.createProduct(body);
      }
      navigate('/products');
    } catch (e) {
      setError((e as Error).message);
    }
  };

  return (
    <section>
      <h2>{id ? '商品編集' : '商品登録'}</h2>
      {error && <p className="error">{error}</p>}
      <form className="stack" onSubmit={submit}>
        <label>
          SKU
          <input value={sku} onChange={(e) => setSku(e.target.value)} required />
        </label>
        <label>
          商品名
          <input value={name} onChange={(e) => setName(e.target.value)} required />
        </label>
        <label>
          価格 (円)
          <input
            type="number"
            min="0"
            step="1"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
            required
          />
        </label>
        <label>
          在庫数
          <input
            type="number"
            min="0"
            step="1"
            value={stockQuantity}
            onChange={(e) => setStockQuantity(e.target.value)}
            required
          />
        </label>
        <div>
          <button type="submit">保存</button>
          <button type="button" onClick={() => navigate('/products')}>
            キャンセル
          </button>
        </div>
      </form>
    </section>
  );
}
