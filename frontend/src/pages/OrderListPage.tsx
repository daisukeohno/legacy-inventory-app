import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchOrders } from '../api/client'
import type { Order } from '../api/types'
import { formatQuantity, formatYen } from '../format'

export default function OrderListPage() {
  const [orders, setOrders] = useState<Order[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    void (async () => {
      try {
        setOrders(await fetchOrders())
      } catch (e) {
        setError(e instanceof Error ? e.message : '注文の取得に失敗しました。')
      }
    })()
  }, [])

  return (
    <section>
      <div className="page-head">
        <h2>注文一覧</h2>
        <Link className="button" to="/orders/new">
          新規注文
        </Link>
      </div>

      {error !== null && <p className="error">{error}</p>}

      {orders.map((order) => (
        <article key={order.id} className="card" data-testid="order-card">
          <header className="card-head">
            <div>
              <strong>注文 #{order.id}</strong> / {order.customerName}
            </div>
            <div>
              <span className="badge">{order.status}</span> {order.orderDate}
            </div>
          </header>
          <table>
            <thead>
              <tr>
                <th>商品名</th>
                <th className="num">単価</th>
                <th className="num">数量</th>
                <th className="num">小計</th>
              </tr>
            </thead>
            <tbody>
              {order.items.map((item, index) => (
                <tr key={item.id ?? index}>
                  <td>{item.productName}</td>
                  <td className="num">{formatYen(item.unitPrice)}</td>
                  <td className="num">{formatQuantity(item.quantity)}</td>
                  <td className="num">{formatYen(item.subtotal)}</td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr>
                <td colSpan={3}>合計</td>
                <td className="num" data-testid="order-total">
                  {formatYen(order.totalAmount)}
                </td>
              </tr>
            </tfoot>
          </table>
        </article>
      ))}
      {orders.length === 0 && error === null && <p>注文はまだありません。</p>}
    </section>
  )
}
