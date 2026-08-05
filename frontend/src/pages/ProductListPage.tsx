import { useCallback, useEffect, useState } from 'react'
import { fetchProducts, type Product } from '../api'
import { formatNumber, formatYen } from '../format'

type Props = {
  onCreate: () => void
  onEdit: (productId: number) => void
}

export function ProductListPage({ onCreate, onEdit }: Props) {
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
      setError(e instanceof Error ? e.message : '商品一覧の取得に失敗しました。')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void load('', false)
  }, [load])

  return (
    <section>
      <div className="page-head">
        <h2>商品(在庫)一覧</h2>
        <button type="button" className="primary" onClick={onCreate}>
          ＋新規商品登録
        </button>
      </div>

      <form
        className="search-bar"
        onSubmit={(event) => {
          event.preventDefault()
          void load(keyword, lowStockOnly)
        }}
      >
        <label>
          キーワード
          <input
            type="search"
            value={keyword}
            placeholder="商品名 / SKU"
            onChange={(event) => setKeyword(event.target.value)}
          />
        </label>
        <label className="checkbox">
          <input
            type="checkbox"
            checked={lowStockOnly}
            onChange={(event) => setLowStockOnly(event.target.checked)}
          />
          在庫少のみ
        </label>
        <button type="submit">検索</button>
      </form>

      {error && <p className="error-box">{error}</p>}

      <div className="table-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th>SKU</th>
              <th>商品名</th>
              <th className="numeric">単価</th>
              <th className="numeric">在庫数</th>
              <th aria-label="操作" />
            </tr>
          </thead>
          <tbody>
            {products.map((product) => (
              <tr key={product.id} className={product.lowStock ? 'low-stock-row' : undefined}>
                <td data-label="SKU">{product.sku}</td>
                <td data-label="商品名">{product.name}</td>
                <td data-label="単価" className="numeric">{formatYen(product.price)}</td>
                <td data-label="在庫数" className="numeric">
                  {product.lowStock ? (
                    <span className="badge low-stock">
                      {formatNumber(product.stockQuantity)} 在庫少
                    </span>
                  ) : (
                    <span className="badge">{formatNumber(product.stockQuantity)}</span>
                  )}
                </td>
                <td data-label="操作">
                  <button type="button" className="link" onClick={() => onEdit(product.id)}>
                    編集
                  </button>
                </td>
              </tr>
            ))}
            {!loading && products.length === 0 && (
              <tr>
                <td colSpan={5} className="empty">
                  該当する商品がありません。
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  )
}
