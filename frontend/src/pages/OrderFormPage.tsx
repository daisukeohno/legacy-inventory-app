import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api/client'
import type { Product } from '../api/types'
import ErrorBanner from '../components/ErrorBanner'
import StockCell from '../components/StockCell'
import { formatYen } from '../utils/format'

export default function OrderFormPage() {
  const navigate = useNavigate()
  const [products, setProducts] = useState<Product[]>([])
  const [customerName, setCustomerName] = useState('')
  const [quantities, setQuantities] = useState<Record<number, string>>({})
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    api
      .listProducts('', false)
      .then(setProducts)
      .catch((e: unknown) => setError(e instanceof Error ? e.message : String(e)))
  }, [])

  const items = products
    .map((product) => ({ productId: product.id, quantity: Number(quantities[product.id] ?? '0') }))
    .filter((item) => Number.isInteger(item.quantity) && item.quantity > 0)

  async function submit(event: React.FormEvent) {
    event.preventDefault()
    if (customerName.trim() === '') {
      setError('得意先名を入力してください。')
      return
    }
    if (items.length === 0) {
      setError('少なくとも1つの商品を数量1以上で選択してください。')
      return
    }
    setSubmitting(true)
    try {
      await api.createOrder({ customerName: customerName.trim(), items })
      navigate('/orders')
    } catch (e) {
      // 在庫不足(HTTP 409)などのAPIエラーメッセージをそのまま表示する
      setError(e instanceof Error ? e.message : String(e))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section>
      <h2>新規注文</h2>
      <ErrorBanner message={error} />

      <form onSubmit={submit}>
        <div className="form">
          <label>
            得意先名
            <input
              type="text"
              name="customerName"
              value={customerName}
              onChange={(event) => setCustomerName(event.target.value)}
            />
          </label>
        </div>

        <h3>商品を選択</h3>
        <table className="data-table">
          <thead>
            <tr>
              <th>SKU</th>
              <th>商品名</th>
              <th className="numeric">単価</th>
              <th className="numeric">在庫数</th>
              <th className="numeric">注文数量</th>
              <th className="numeric">小計</th>
            </tr>
          </thead>
          <tbody>
            {products.map((product) => {
              const quantity = Number(quantities[product.id] ?? '0')
              const subtotal = Number.isFinite(quantity) ? product.price * Math.max(quantity, 0) : 0
              return (
                <tr key={product.id} className={product.lowStock ? 'low-stock-row' : undefined}>
                  <td>{product.sku}</td>
                  <td>{product.name}</td>
                  <td className="numeric">{formatYen(product.price)}</td>
                  <td className="numeric">
                    <StockCell stockQuantity={product.stockQuantity} lowStock={product.lowStock} />
                  </td>
                  <td className="numeric">
                    <input
                      type="number"
                      min="0"
                      className="quantity-input"
                      aria-label={`${product.name} の注文数量`}
                      value={quantities[product.id] ?? '0'}
                      onChange={(event) =>
                        setQuantities((current) => ({ ...current, [product.id]: event.target.value }))
                      }
                    />
                  </td>
                  <td className="numeric">{formatYen(subtotal)}</td>
                </tr>
              )
            })}
          </tbody>
        </table>

        <div className="form-actions">
          <button type="submit" disabled={submitting}>
            注文確定
          </button>
          <Link className="button-link" to="/orders">
            キャンセル
          </Link>
        </div>
      </form>
    </section>
  )
}
