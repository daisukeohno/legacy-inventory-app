import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'
import type { Product } from '../api/types'
import ErrorBanner from '../components/ErrorBanner'
import StockCell from '../components/StockCell'
import { formatYen } from '../utils/format'

export default function ProductListPage() {
  const [keyword, setKeyword] = useState('')
  const [lowStockOnly, setLowStockOnly] = useState(false)
  const [products, setProducts] = useState<Product[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const load = useCallback(async (searchKeyword: string, onlyLowStock: boolean) => {
    setLoading(true)
    try {
      setProducts(await api.listProducts(searchKeyword, onlyLowStock))
      setError(null)
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void load(keyword, lowStockOnly)
    // 初回 + 低在庫フィルタ変更時に再取得する(キーワードは検索ボタンで反映)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [lowStockOnly, load])

  return (
    <section>
      <h2>商品(在庫)一覧</h2>
      <ErrorBanner message={error} />

      <form
        className="toolbar"
        onSubmit={(event) => {
          event.preventDefault()
          void load(keyword, lowStockOnly)
        }}
      >
        <label>
          キーワード:{' '}
          <input
            type="text"
            name="keyword"
            value={keyword}
            placeholder="商品名 / SKU"
            onChange={(event) => setKeyword(event.target.value)}
          />
        </label>
        <label>
          <input
            type="checkbox"
            name="lowStockOnly"
            checked={lowStockOnly}
            onChange={(event) => setLowStockOnly(event.target.checked)}
          />{' '}
          在庫少のみ
        </label>
        <button type="submit">検索</button>
        <Link className="button-link" to="/products/new">
          ＋新規商品登録
        </Link>
      </form>

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
                <StockCell stockQuantity={product.stockQuantity} lowStock={product.lowStock} />
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
