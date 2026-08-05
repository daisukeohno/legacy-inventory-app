import { useEffect, useMemo, useState } from 'react'
import { createOrder, fetchProducts, type Product } from '../api'
import { formatNumber, formatYen } from '../format'

type Props = {
  onDone: () => void
}

export function OrderFormPage({ onDone }: Props) {
  const [customerName, setCustomerName] = useState('')
  const [products, setProducts] = useState<Product[]>([])
  const [quantities, setQuantities] = useState<Record<number, string>>({})
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    fetchProducts('', false)
      .then(setProducts)
      .catch((e: unknown) =>
        setError(e instanceof Error ? e.message : '商品一覧の取得に失敗しました。'),
      )
  }, [])

  const total = useMemo(
    () =>
      products.reduce((sum, product) => {
        const quantity = Number(quantities[product.id] ?? 0)
        return Number.isInteger(quantity) && quantity > 0 ? sum + quantity * product.price : sum
      }, 0),
    [products, quantities],
  )

  const submit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!customerName.trim()) {
      setError('得意先名を入力してください。')
      return
    }

    const items = products
      .map((product) => ({ productId: product.id, quantity: Number(quantities[product.id] ?? 0) }))
      .filter((item) => Number.isInteger(item.quantity) && item.quantity > 0)

    if (items.length === 0) {
      setError('少なくとも1つの商品を数量1以上で選択してください。')
      return
    }

    setSaving(true)
    try {
      await createOrder({ customerName: customerName.trim(), items })
      onDone()
    } catch (e) {
      setError(e instanceof Error ? e.message : '注文の確定に失敗しました。')
    } finally {
      setSaving(false)
    }
  }

  return (
    <section>
      <h2>新規注文</h2>

      {error && <p className="error-box">{error}</p>}

      <form onSubmit={submit} noValidate>
        <div className="form">
          <label>
            得意先名
            <input
              type="text"
              value={customerName}
              onChange={(event) => setCustomerName(event.target.value)}
            />
          </label>
        </div>

        <h3>商品を選択</h3>
        <div className="table-wrapper">
          <table className="data-table">
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
                  <td data-label="注文数量" className="numeric">
                    <input
                      type="number"
                      min={0}
                      step={1}
                      className="qty"
                      value={quantities[product.id] ?? '0'}
                      onChange={(event) =>
                        setQuantities({ ...quantities, [product.id]: event.target.value })
                      }
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <p className="order-total">合計金額: {formatYen(total)}</p>

        <div className="form-actions">
          <button type="submit" className="primary" disabled={saving}>
            注文確定
          </button>
          <button type="button" className="link" onClick={onDone}>
            キャンセル
          </button>
        </div>
      </form>
    </section>
  )
}
