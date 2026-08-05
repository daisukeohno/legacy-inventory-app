import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, type Order } from '../api';
import { formatYen } from '../format';

export default function OrderListPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api
      .listOrders()
      .then(setOrders)
      .catch((e: Error) => setError(e.message));
  }, []);

  return (
    <section>
      <h2>注文一覧</h2>
      <div className="toolbar">
        <Link className="button" to="/orders/new">
          新規注文
        </Link>
      </div>
      {error && <p className="error">{error}</p>}
      {orders.map((order) => (
        <article key={order.id} className="card" data-testid="order-card">
          <header>
            <strong>#{order.id}</strong> {order.customerName} / {order.orderDate} /{' '}
            <span className="status">{order.status}</span>
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
              {order.items.map((item) => (
                <tr key={`${order.id}-${item.productId}`}>
                  <td>{item.productName}</td>
                  <td className="num">{formatYen(item.unitPrice)}</td>
                  <td className="num">{item.quantity}</td>
                  <td className="num">{formatYen(item.subtotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <p className="total">合計: {formatYen(order.totalAmount)}</p>
        </article>
      ))}
      {orders.length === 0 && <p>注文がありません。</p>}
    </section>
  );
}
