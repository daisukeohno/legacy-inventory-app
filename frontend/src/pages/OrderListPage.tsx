import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchOrders } from '../api/client'
import type { Order } from '../api/types'
import { formatYen } from '../format'

export default function OrderListPage() {
  const [orders, setOrders] = useState<Order[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchOrders()
      .then(setOrders)
      .catch((e: Error) => setError(e.message))
  }, [])

  return (
    <section>
      <h2>注文一覧</h2>
      <div className="toolbar">
        <Link className="button-link" to="/orders/new">
          ＋新規注文
        </Link>
      </div>
      {error !== null && <p className="error">{error}</p>}
      <table className="data-table">
        <thead>
          <tr>
            <th>注文番号</th>
            <th>得意先</th>
            <th>注文日</th>
            <th>状態</th>
            <th>明細</th>
            <th className="numeric">合計金額</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((order) => (
            <tr key={order.id}>
              <td>{order.id}</td>
              <td>{order.customer.name}</td>
              <td>{order.orderDate}</td>
              <td>{order.status}</td>
              <td>
                <ul className="item-list">
                  {order.items.map((item) => (
                    <li key={item.productId}>
                      {item.productName} × {item.quantity} = {formatYen(item.subtotal)}
                    </li>
                  ))}
                </ul>
              </td>
              <td className="numeric total">{formatYen(order.totalAmount)}</td>
            </tr>
          ))}
          {orders.length === 0 && (
            <tr>
              <td colSpan={6}>注文がありません。</td>
            </tr>
          )}
        </tbody>
      </table>
    </section>
  )
}
