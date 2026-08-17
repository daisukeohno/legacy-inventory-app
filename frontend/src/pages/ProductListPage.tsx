import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchProducts } from '../api/client'
import type { Product } from '../api/types'
import { formatQuantity, formatYen } from '../format'

export default function ProductListPage() {
  const [keyword, setKeyword] = useState('')
  const [lowStockOnly, setLowStockOnly] = useState(false)
  const [products, setProducts] = useState<Product[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const load = useCallback(async (searchKeyword: string, onlyLowStock: boolean) => {
    setLoading(true)
    try {
      setProducts(await fetchProducts(searchKeyword, onlyLowStock))
      setError(null)
    } catch (e) {
      setError(e instanceof Error ? e.message : '商品の取得に失敗しました。')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void load(keyword, lowStockOnly)
    // 初回のみ。以降は検索ボタン/フィルタ操作で再取得する。
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <section>
      <div className="page-head">
        <h2>商品一覧</h2>
        <Link className="button" to="/products/new">
          商品を登録
        </Link>
      </div>

      <form
        className="filter-bar"
        onSubmit={(event) => {
          event.preventDefault()
          void load(keyword, lowStockOnly)
        }}
      >
        <input
          type="search"
          aria-label="キーワード"
          placeholder="商品名 / SKU で検索"
          value={keyword}
          onChange={(event) => setKeyword(event.target.value)}
        />
        <label className="checkbox">
          <input
            type="checkbox"
            checked={lowStockOnly}
            onChange={(event) => {
              const next = event.target.checked
              setLowStockOnly(next)
              void load(keyword, next)
            }}
          />
          低在庫のみ表示
        </label>
        <button type="submit">検索</button>
      </form>

      {error !== null && <p className="error">{error}</p>}
      {loading && <p>読み込み中…</p>}

      <table>
        <thead>
          <tr>
            <th>SKU</th>
            <th>商品名</th>
            <th className="num">価格</th>
            <th className="num">在庫数</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          {products.map((product) => (
            <tr key={product.id} data-testid="product-row" data-sku={product.sku}>
              <td>{product.sku}</td>
              <td>{product.name}</td>
              <td className="num">{formatYen(product.price)}</td>
              <td className="num">
                {formatQuantity(product.stockQuantity)}
                {product.lowStock && (
                  <span className="badge badge-warning" data-testid="low-stock-badge">
                    ⚠ 在庫少
                  </span>
                )}
              </td>
              <td>
                <Link to={`/products/${product.id}/edit`}>編集</Link>
              </td>
            </tr>
          ))}
          {!loading && products.length === 0 && (
            <tr>
              <td colSpan={5}>該当する商品はありません。</td>
            </tr>
          )}
        </tbody>
      </table>
    </section>
  )
}
