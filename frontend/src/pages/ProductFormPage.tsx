import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { createProduct, fetchProduct, updateProduct } from '../api/client'

export default function ProductFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const productId = id === undefined ? null : Number(id)

  const [sku, setSku] = useState('')
  const [name, setName] = useState('')
  const [price, setPrice] = useState('')
  const [stockQuantity, setStockQuantity] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (productId === null) return
    fetchProduct(productId)
      .then((product) => {
        setSku(product.sku)
        setName(product.name)
        setPrice(String(product.price))
        setStockQuantity(String(product.stockQuantity))
      })
      .catch((e: Error) => setError(e.message))
  }, [productId])

  const submit = async (event: React.FormEvent) => {
    event.preventDefault()
    setSaving(true)
    setError(null)
    const input = {
      sku,
      name,
      price: Number(price),
      stockQuantity: Number(stockQuantity),
    }
    try {
      if (productId === null) {
        await createProduct(input)
      } else {
        await updateProduct(productId, input)
      }
      navigate('/products')
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <section>
      <h2>{productId === null ? '商品登録' : '商品編集'}</h2>
      {error !== null && <p className="error">{error}</p>}
      <form className="form" onSubmit={submit}>
        <label>
          SKU
          <input value={sku} onChange={(e) => setSku(e.target.value)} required />
        </label>
        <label>
          商品名
          <input value={name} onChange={(e) => setName(e.target.value)} required />
        </label>
        <label>
          単価(円)
          <input
            type="number"
            min={0}
            step={1}
            value={price}
            onChange={(e) => setPrice(e.target.value)}
            required
          />
        </label>
        <label>
          在庫数
          <input
            type="number"
            min={0}
            step={1}
            value={stockQuantity}
            onChange={(e) => setStockQuantity(e.target.value)}
            required
          />
        </label>
        <div className="form-actions">
          <button type="submit" disabled={saving}>
            保存
          </button>
          <button type="button" onClick={() => navigate('/products')}>
            キャンセル
          </button>
        </div>
      </form>
    </section>
  )
}
