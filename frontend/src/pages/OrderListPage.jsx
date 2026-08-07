import { useEffect, useState } from 'react';
import { fetchOrders } from '../api.js';
import { formatYen } from '../format.js';

export default function OrderListPage() {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchOrders()
      .then(setOrders)
      .catch((e) => setError(e.message));
  }, []);

  return (
    <section>
      <h2>注文一覧</h2>
      {error && <p role="alert" className="error">{error}</p>}
      {orders.map((order) => (
        <article key={order.id} className="order-card" data-testid="order-card">
          <header>
            <strong>#{order.id}</strong> {order.customerName} / {order.orderDate} / {order.status}
          </header>
          <table className="data-table">
            <thead>
              <tr>
                <th>商品名</th>
                <th className="numeric">単価</th>
                <th className="numeric">数量</th>
                <th className="numeric">小計</th>
              </tr>
            </thead>
            <tbody>
              {order.items.map((item) => (
                <tr key={`${order.id}-${item.productId}`}>
                  <td>{item.productName}</td>
                  <td className="numeric">{formatYen(item.unitPrice)}</td>
                  <td className="numeric">{item.quantity}</td>
                  <td className="numeric">{formatYen(item.subtotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <p className="total">合計: {formatYen(order.totalAmount)}</p>
        </article>
      ))}
      {orders.length === 0 && !error && <p>注文がありません。</p>}
    </section>
  );
}
