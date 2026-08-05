import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, type Product } from '../api';
import { formatYen } from '../format';

export default function ProductListPage() {
  const [keyword, setKeyword] = useState('');
  const [lowStockOnly, setLowStockOnly] = useState(false);
  const [products, setProducts] = useState<Product[]>([]);
  const [error, setError] = useState<string | null>(null);

  const load = (kw: string, low: boolean) => {
    api
      .listProducts(kw, low)
      .then(setProducts)
      .catch((e: Error) => setError(e.message));
  };

  useEffect(() => {
    load('', false);
  }, []);

  return (
    <section>
      <h2>商品一覧</h2>
      <form
        className="toolbar"
        onSubmit={(e) => {
          e.preventDefault();
          setError(null);
          load(keyword, lowStockOnly);
        }}
      >
        <input
          aria-label="キーワード"
          placeholder="商品名 / SKU で検索"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        <label>
          <input
            type="checkbox"
            checked={lowStockOnly}
            onChange={(e) => setLowStockOnly(e.target.checked)}
          />
          在庫少のみ表示
        </label>
        <button type="submit">検索</button>
        <Link className="button" to="/products/new">
          商品を登録
        </Link>
      </form>

      {error && <p className="error">{error}</p>}

      <table>
        <thead>
          <tr>
            <th>SKU</th>
            <th>商品名</th>
            <th className="num">価格</th>
            <th className="num">在庫数</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {products.map((p) => (
            <tr key={p.id} className={p.lowStock ? 'low-stock' : undefined} data-testid="product-row">
              <td>{p.sku}</td>
              <td>{p.name}</td>
              <td className="num">{formatYen(p.price)}</td>
              <td className="num">
                {p.stockQuantity}
                {p.lowStock && <span className="badge">在庫少</span>}
              </td>
              <td>
                <Link to={`/products/${p.id}`} state={p}>
                  編集
                </Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {products.length === 0 && <p>該当する商品がありません。</p>}
    </section>
  );
}
