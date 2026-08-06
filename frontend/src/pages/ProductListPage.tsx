import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchProducts } from '../api';
import { formatQuantity, formatYen } from '../format';
import type { Product } from '../types';

export function ProductListPage() {
  const [keyword, setKeyword] = useState('');
  const [lowStockOnly, setLowStockOnly] = useState(false);
  const [products, setProducts] = useState<Product[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async (searchKeyword: string, onlyLowStock: boolean) => {
    setLoading(true);
    setError(null);
    try {
      setProducts(await fetchProducts(searchKeyword, onlyLowStock));
    } catch (e) {
      setError(e instanceof Error ? e.message : '商品一覧の取得に失敗しました。');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load(keyword, lowStockOnly);
    // 低在庫フィルタはチェック変更で即時反映し、キーワードは検索ボタン/Enterで反映する
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [lowStockOnly, load]);

  return (
    <section>
      <h2>商品一覧</h2>
      {error && <p className="error" role="alert">{error}</p>}
      <form
        className="card filters"
        onSubmit={(event) => {
          event.preventDefault();
          void load(keyword, lowStockOnly);
        }}
      >
        <label htmlFor="keyword">キーワード(商品名・SKU)</label>
        <input
          id="keyword"
          type="text"
          value={keyword}
          placeholder="例: マウス / SKU-1002"
          onChange={(event) => setKeyword(event.target.value)}
        />
        <label>
          <input
            type="checkbox"
            checked={lowStockOnly}
            onChange={(event) => setLowStockOnly(event.target.checked)}
          />
          低在庫のみ表示
        </label>
        <button type="submit" disabled={loading}>検索</button>
      </form>

      <div className="card">
        <table>
          <thead>
            <tr>
              <th>SKU</th>
              <th>商品名</th>
              <th className="numeric">単価</th>
              <th className="numeric">在庫数</th>
              <th>状態</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            {products.map((product) => (
              <tr key={product.id} className={product.lowStock ? 'low-stock' : undefined} data-testid="product-row">
                <td>{product.sku}</td>
                <td>{product.name}</td>
                <td className="numeric">{formatYen(product.price)}</td>
                <td className="numeric">{formatQuantity(product.stockQuantity)}</td>
                <td>{product.lowStock && <span className="badge">低在庫</span>}</td>
                <td>
                  <Link to={`/products/${product.id}/edit`}>編集</Link>
                </td>
              </tr>
            ))}
            {products.length === 0 && !loading && (
              <tr>
                <td colSpan={6}>該当する商品がありません。</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}
