import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'
import type { Order } from '../api/types'
import ErrorBanner from '../components/ErrorBanner'
import { formatYen } from '../utils/format'

export default function OrderListPage() {
  const [orders, setOrders] = useState<Order[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api
      .listOrders()
      .then(setOrders)
      .catch((e: unknown) => setError(e instanceof Error ? e.message : String(e)))
  }, [])

  return (
    <section>
      <h2>注文一覧</h2>
      <ErrorBanner message={error} />

      <div className="toolbar">
        <Link className="button-link" to="/orders/new">
          ＋新規注文
        </Link>
      </div>

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
              <td>{order.customerName}</td>
              <td>{order.orderDate}</td>
              <td>
                <span className="status-badge">{order.status}</span>
              </td>
              <td>
                <ul className="item-list">
                  {order.items.map((item) => (
                    <li key={item.id}>
                      {item.productName} × {item.quantity} = {formatYen(item.subtotal)}
                    </li>
                  ))}
                </ul>
              </td>
              <td className="numeric total-amount">{formatYen(order.totalAmount)}</td>
            </tr>
          ))}
          {orders.length === 0 && (
            <tr>
              <td colSpan={6}>注文はありません。</td>
            </tr>
          )}
        </tbody>
      </table>
    </section>
  )
}
