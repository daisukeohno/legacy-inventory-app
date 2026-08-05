import { useEffect, useState } from 'react'
import { fetchOrders, type Order } from '../api'
import { formatYen } from '../format'

type Props = {
  onCreate: () => void
}

export function OrderListPage({ onCreate }: Props) {
  const [orders, setOrders] = useState<Order[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchOrders()
      .then(setOrders)
      .catch((e: unknown) =>
        setError(e instanceof Error ? e.message : '注文一覧の取得に失敗しました。'),
      )
  }, [])

  return (
    <section>
      <div className="page-head">
        <h2>注文一覧</h2>
        <button type="button" className="primary" onClick={onCreate}>
          ＋新規注文
        </button>
      </div>

      {error && <p className="error-box">{error}</p>}

      <div className="table-wrapper">
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
                <td data-label="注文番号">{order.id}</td>
                <td data-label="得意先">{order.customerName}</td>
                <td data-label="注文日">{order.orderDate}</td>
                <td data-label="状態">
                  <span className={`status status-${order.status.toLowerCase()}`}>
                    {order.status}
                  </span>
                </td>
                <td data-label="明細">
                  <ul className="item-list">
                    {order.items.map((item, index) => (
                      <li key={`${order.id}-${index}`}>
                        <span>{item.productName}</span>
                        <span>× {item.quantity}</span>
                        <span>= {formatYen(item.subtotal)}</span>
                      </li>
                    ))}
                  </ul>
                </td>
                <td data-label="合計金額" className="numeric total">
                  {formatYen(order.totalAmount)}
                </td>
              </tr>
            ))}
            {orders.length === 0 && (
              <tr>
                <td colSpan={6} className="empty">
                  注文がありません。
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  )
}
