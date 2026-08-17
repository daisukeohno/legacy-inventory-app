import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createOrder, fetchProducts } from '../api/client'
import type { Product } from '../api/types'
import { formatYen } from '../format'

export default function OrderCreatePage() {
  const navigate = useNavigate()
  const [products, setProducts] = useState<Product[]>([])
  const [customerName, setCustomerName] = useState('')
  const [quantities, setQuantities] = useState<Record<number, string>>({})
  const [message, setMessage] = useState<string | null>(null)

  const loadProducts = useCallback(async () => {
    try {
      setProducts(await fetchProducts('', false))
    } catch (e) {
      setMessage(e instanceof Error ? e.message : '商品の取得に失敗しました。')
    }
  }, [])

  useEffect(() => {
    void loadProducts()
  }, [loadProducts])

  const lines = useMemo(
    () =>
      products
        .map((product) => ({ product, quantity: Number(quantities[product.id] ?? '') }))
        .filter((line) => Number.isInteger(line.quantity) && line.quantity > 0),
    [products, quantities],
  )

  const total = lines.reduce((sum, line) => sum + line.product.price * line.quantity, 0)

  const submit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (customerName.trim() === '') {
      setMessage('得意先名を入力してください。')
      return
    }
    if (lines.length === 0) {
      setMessage('少なくとも1つの商品を数量1以上で選択してください。')
      return
    }

    try {
      await createOrder({
        customerName: customerName.trim(),
        items: lines.map((line) => ({ productId: line.product.id, quantity: line.quantity })),
      })
      navigate('/orders')
    } catch (e) {
      setMessage(e instanceof Error ? e.message : '注文の登録に失敗しました。')
      // 在庫不足などで失敗したときは最新在庫を表示に反映する。
      await loadProducts()
    }
  }

  return (
    <section>
      <h2>新規注文</h2>
      {message !== null && (
        <p className="error" data-testid="order-error">
          {message}
        </p>
      )}
      <form onSubmit={submit} noValidate>
        <label className="inline-label">
          得意先名
          <input value={customerName} onChange={(e) => setCustomerName(e.target.value)} />
        </label>

        <table>
          <thead>
            <tr>
              <th>SKU</th>
              <th>商品名</th>
              <th className="num">単価</th>
              <th className="num">在庫数</th>
              <th className="num">数量</th>
            </tr>
          </thead>
          <tbody>
            {products.map((product) => (
              <tr key={product.id}>
                <td>{product.sku}</td>
                <td>{product.name}</td>
                <td className="num">{formatYen(product.price)}</td>
                <td className="num">
                  {product.stockQuantity}
                  {product.lowStock && <span className="badge badge-warning">⚠ 在庫少</span>}
                </td>
                <td className="num">
                  <input
                    className="qty"
                    aria-label={`${product.name} の数量`}
                    value={quantities[product.id] ?? ''}
                    onChange={(e) =>
                      setQuantities((current) => ({ ...current, [product.id]: e.target.value }))
                    }
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        <p className="total">
          合計: <strong data-testid="client-total">{formatYen(total)}</strong>
        </p>

        <div className="actions">
          <button type="submit">注文を確定</button>
          <button type="button" className="secondary" onClick={() => navigate('/orders')}>
            キャンセル
          </button>
        </div>
      </form>
    </section>
  )
}
