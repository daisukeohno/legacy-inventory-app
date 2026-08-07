import { NavLink, Navigate, Route, Routes } from 'react-router-dom';
import ProductListPage from './pages/ProductListPage.jsx';
import ProductEditPage from './pages/ProductEditPage.jsx';
import OrderListPage from './pages/OrderListPage.jsx';
import OrderCreatePage from './pages/OrderCreatePage.jsx';

export default function App() {
  return (
    <div className="app">
      <header className="app-header">
        <h1>在庫・注文管理</h1>
        <nav>
          <NavLink to="/products">商品一覧</NavLink>
          <NavLink to="/products/new">商品登録</NavLink>
          <NavLink to="/orders">注文一覧</NavLink>
          <NavLink to="/orders/new">新規注文</NavLink>
        </nav>
      </header>
      <main>
        <Routes>
          <Route path="/" element={<Navigate to="/products" replace />} />
          <Route path="/products" element={<ProductListPage />} />
          <Route path="/products/new" element={<ProductEditPage />} />
          <Route path="/products/:id/edit" element={<ProductEditPage />} />
          <Route path="/orders" element={<OrderListPage />} />
          <Route path="/orders/new" element={<OrderCreatePage />} />
        </Routes>
      </main>
    </div>
  );
}
