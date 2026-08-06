import { useEffect, useState } from 'react';
import { fetchOrders } from '../api';
import { formatDate, formatQuantity, formatYen } from '../format';
import type { Order } from '../types';

export function OrderListPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchOrders()
      .then(setOrders)
      .catch((e: unknown) => setError(e instanceof Error ? e.message : '注文一覧の取得に失敗しました。'));
  }, []);

  return (
    <section>
      <h2>注文一覧</h2>
      {error && <p className="error" role="alert">{error}</p>}
      {orders.map((order) => (
        <div className="card" key={order.id} data-testid="order-card">
          <h3>
            注文番号 {order.id} / {order.customerName}
          </h3>
          <p>
            注文日: {formatDate(order.orderDate)} ／ ステータス: {order.status}
          </p>
          <table>
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
                <tr key={item.id}>
                  <td>{item.productName}</td>
                  <td className="numeric">{formatYen(item.unitPrice)}</td>
                  <td className="numeric">{formatQuantity(item.quantity)}</td>
                  <td className="numeric">{formatYen(item.subtotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <p className="total" data-testid="order-total">
            合計金額: {formatYen(order.totalAmount)}
          </p>
        </div>
      ))}
      {orders.length === 0 && !error && <p className="card">注文がありません。</p>}
    </section>
  );
}
