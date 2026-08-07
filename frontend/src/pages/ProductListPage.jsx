import { useCallback, useEffect, useState } from 'react';
import ProductTable from '../components/ProductTable.jsx';
import { fetchProducts } from '../api.js';

export default function ProductListPage() {
  const [keyword, setKeyword] = useState('');
  const [lowStock, setLowStock] = useState(false);
  const [products, setProducts] = useState([]);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    try {
      setProducts(await fetchProducts({ keyword, lowStock }));
      setError('');
    } catch (e) {
      setError(e.message);
    }
  }, [keyword, lowStock]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <section>
      <h2>商品一覧</h2>
      <form
        className="filters"
        onSubmit={(event) => {
          event.preventDefault();
          load();
        }}
      >
        <label>
          キーワード
          <input
            type="search"
            value={keyword}
            aria-label="キーワード"
            onChange={(event) => setKeyword(event.target.value)}
          />
        </label>
        <label>
          <input
            type="checkbox"
            checked={lowStock}
            aria-label="在庫が少ない商品のみ"
            onChange={(event) => setLowStock(event.target.checked)}
          />
          在庫が少ない商品のみ
        </label>
        <button type="submit">検索</button>
      </form>
      {error && <p role="alert" className="error">{error}</p>}
      <ProductTable products={products} />
    </section>
  );
}
