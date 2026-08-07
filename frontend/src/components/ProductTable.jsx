import { Link } from 'react-router-dom';
import { formatYen } from '../format.js';

export default function ProductTable({ products }) {
  return (
    <table className="data-table">
      <thead>
        <tr>
          <th>SKU</th>
          <th>商品名</th>
          <th className="numeric">価格</th>
          <th className="numeric">在庫数</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        {products.map((product) => (
          <tr key={product.id} data-testid="product-row">
            <td>{product.sku}</td>
            <td>{product.name}</td>
            <td className="numeric">{formatYen(product.price)}</td>
            <td className="numeric">
              {product.stockQuantity}
              {product.lowStock && (
                <span className="badge badge-low-stock" data-testid="low-stock-badge">
                  在庫少
                </span>
              )}
            </td>
            <td>
              <Link to={`/products/${product.id}/edit`}>編集</Link>
            </td>
          </tr>
        ))}
        {products.length === 0 && (
          <tr>
            <td colSpan="5">該当する商品がありません。</td>
          </tr>
        )}
      </tbody>
    </table>
  );
}
