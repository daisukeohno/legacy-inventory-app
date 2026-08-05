import { useState } from 'react'
import './App.css'
import { OrderFormPage } from './pages/OrderFormPage'
import { OrderListPage } from './pages/OrderListPage'
import { ProductFormPage } from './pages/ProductFormPage'
import { ProductListPage } from './pages/ProductListPage'

type View =
  | { name: 'productList' }
  | { name: 'productForm'; productId?: number }
  | { name: 'orderList' }
  | { name: 'orderForm' }

export default function App() {
  const [view, setView] = useState<View>({ name: 'productList' })

  return (
    <div className="app">
      <header className="app-header">
        <h1>在庫・注文管理システム</h1>
        <nav>
          <button
            type="button"
            className={view.name.startsWith('product') ? 'nav-link active' : 'nav-link'}
            onClick={() => setView({ name: 'productList' })}
          >
            商品(在庫)
          </button>
          <button
            type="button"
            className={view.name.startsWith('order') ? 'nav-link active' : 'nav-link'}
            onClick={() => setView({ name: 'orderList' })}
          >
            注文
          </button>
        </nav>
      </header>

      <main className="app-main">
        {view.name === 'productList' && (
          <ProductListPage
            onCreate={() => setView({ name: 'productForm' })}
            onEdit={(productId) => setView({ name: 'productForm', productId })}
          />
        )}
        {view.name === 'productForm' && (
          <ProductFormPage
            productId={view.productId}
            onDone={() => setView({ name: 'productList' })}
          />
        )}
        {view.name === 'orderList' && (
          <OrderListPage onCreate={() => setView({ name: 'orderForm' })} />
        )}
        {view.name === 'orderForm' && (
          <OrderFormPage onDone={() => setView({ name: 'orderList' })} />
        )}
      </main>
    </div>
  )
}
