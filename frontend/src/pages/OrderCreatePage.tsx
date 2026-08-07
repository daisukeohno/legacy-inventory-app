import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createOrder, fetchCustomers, fetchProducts } from '../api/client'
import type { Customer, Product } from '../api/types'
import { formatYen } from '../format'

export default function OrderCreatePage() {
  const navigate = useNavigate()
  const [customers, setCustomers] = useState<Customer[]>([])
  const [products, setProducts] = useState<Product[]>([])
  const [customerId, setCustomerId] = useState('')
  const [quantities, setQuantities] = useState<Record<number, string>>({})
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    Promise.all([fetchCustomers(), fetchProducts('', false)])
      .then(([customerList, productList]) => {
        setCustomers(customerList)
        setProducts(productList)
      })
      .catch((e: Error) => setError(e.message))
  }, [])

  const items = products
    .map((product) => ({ productId: product.id, quantity: Number(quantities[product.id] ?? '0') }))
    .filter((item) => Number.isFinite(item.quantity) && item.quantity > 0)

  const total = items.reduce((sum, item) => {
    const product = products.find((p) => p.id === item.productId)
    return sum + (product === undefined ? 0 : product.price * item.quantity)
  }, 0)

  const submit = async (event: React.FormEvent) => {
    event.preventDefault()
    setError(null)
    if (customerId === '') {
      setError('得意先を選択してください。')
      return
    }
    if (items.length === 0) {
      setError('少なくとも1つの商品を数量1以上で選択してください。')
      return
    }
    setSubmitting(true)
    try {
      await createOrder({ customerId: Number(customerId), items })
      navigate('/orders')
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section>
      <h2>新規注文</h2>
      {error !== null && (
        <p className="error" role="alert">
          {error}
        </p>
      )}
      <form onSubmit={submit}>
        <label className="form-inline">
          得意先
          <select value={customerId} onChange={(e) => setCustomerId(e.target.value)}>
            <option value="">選択してください</option>
            {customers.map((customer) => (
              <option key={customer.id} value={customer.id}>
                {customer.name}
              </option>
            ))}
          </select>
        </label>

        <h3>商品を選択</h3>
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
                <td>{product.sku}</td>
                <td>{product.name}</td>
                <td className="numeric">{formatYen(product.price)}</td>
                <td className="numeric">
                  {product.stockQuantity}
                  {product.lowStock && <span className="badge">在庫少</span>}
                </td>
                <td className="numeric">
                  <input
                    type="number"
                    min={0}
                    step={1}
                    aria-label={`${product.name} の注文数量`}
                    value={quantities[product.id] ?? '0'}
                    onChange={(e) =>
                      setQuantities({ ...quantities, [product.id]: e.target.value })
                    }
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        <p className="total">合計金額: {formatYen(total)}</p>
        <div className="form-actions">
          <button type="submit" disabled={submitting}>
            注文確定
          </button>
          <button type="button" onClick={() => navigate('/orders')}>
            キャンセル
          </button>
        </div>
      </form>
    </section>
  )
}
