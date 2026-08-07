import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchProducts } from '../api/client'
import type { Product } from '../api/types'
import { formatYen } from '../format'

export default function ProductListPage() {
  const [keyword, setKeyword] = useState('')
  const [lowStockOnly, setLowStockOnly] = useState(false)
  const [products, setProducts] = useState<Product[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState({ keyword: '', lowStockOnly: false })

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    fetchProducts(query.keyword, query.lowStockOnly)
      .then((data) => {
        if (!cancelled) {
          setProducts(data)
          setError(null)
        }
      })
      .catch((e: Error) => !cancelled && setError(e.message))
      .finally(() => !cancelled && setLoading(false))
    return () => {
      cancelled = true
    }
  }, [query])

  return (
    <section>
      <h2>商品(在庫)一覧</h2>
      <form
        className="toolbar"
        onSubmit={(e) => {
          e.preventDefault()
          setQuery({ keyword, lowStockOnly })
        }}
      >
        <label>
          キーワード
          <input
            type="search"
            value={keyword}
            placeholder="商品名 / SKU"
            onChange={(e) => setKeyword(e.target.value)}
          />
        </label>
        <label>
          <input
            type="checkbox"
            checked={lowStockOnly}
            onChange={(e) => setLowStockOnly(e.target.checked)}
          />
          在庫少のみ
        </label>
        <button type="submit">検索</button>
        <Link className="button-link" to="/products/new">
          ＋新規商品登録
        </Link>
      </form>

      {error !== null && <p className="error">{error}</p>}
      {loading && <p>読み込み中...</p>}

      <table className="data-table">
        <thead>
          <tr>
            <th>SKU</th>
            <th>商品名</th>
            <th className="numeric">単価</th>
            <th className="numeric">在庫数</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {products.map((product) => (
            <tr key={product.id} className={product.lowStock ? 'low-stock-row' : undefined}>
              <td>{product.sku}</td>
              <td>{product.name}</td>
              <td className="numeric">{formatYen(product.price)}</td>
              <td className="numeric">
                {product.stockQuantity}
                {product.lowStock && <span className="badge">在庫少</span>}
              </td>
              <td>
                <Link to={`/products/${product.id}/edit`}>編集</Link>
              </td>
            </tr>
          ))}
          {!loading && products.length === 0 && (
            <tr>
              <td colSpan={5}>該当する商品がありません。</td>
            </tr>
          )}
        </tbody>
      </table>
    </section>
  )
}
