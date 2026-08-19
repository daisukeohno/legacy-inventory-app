import { NavLink, Navigate, Route, Routes } from 'react-router-dom'
import OrderFormPage from './pages/OrderFormPage'
import OrderListPage from './pages/OrderListPage'
import ProductFormPage from './pages/ProductFormPage'
import ProductListPage from './pages/ProductListPage'

export default function App() {
  return (
    <div className="app">
      <header className="app-header">
        <h1>在庫・注文管理システム</h1>
        <nav>
          <NavLink to="/products">商品(在庫)一覧</NavLink>
          <NavLink to="/orders">注文一覧</NavLink>
        </nav>
      </header>
      <main>
        <Routes>
          <Route path="/" element={<Navigate to="/products" replace />} />
          <Route path="/products" element={<ProductListPage />} />
          <Route path="/products/new" element={<ProductFormPage />} />
          <Route path="/products/:id/edit" element={<ProductFormPage />} />
          <Route path="/orders" element={<OrderListPage />} />
          <Route path="/orders/new" element={<OrderFormPage />} />
        </Routes>
      </main>
    </div>
  )
}
