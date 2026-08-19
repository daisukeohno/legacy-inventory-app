import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api/client'
import ErrorBanner from '../components/ErrorBanner'

interface FormState {
  sku: string
  name: string
  price: string
  stockQuantity: string
}

const EMPTY: FormState = { sku: '', name: '', price: '', stockQuantity: '' }

/** 旧 ProductForm.validate() と同じチェックをクライアント側でも行う。 */
function validate(form: FormState): string[] {
  const errors: string[] = []
  if (form.sku.trim() === '') {
    errors.push('SKUを入力してください。')
  }
  if (form.name.trim() === '') {
    errors.push('商品名を入力してください。')
  }
  const price = Number(form.price)
  if (form.price.trim() === '' || Number.isNaN(price)) {
    errors.push('単価は数値で入力してください。')
  } else if (price < 0) {
    errors.push('単価は0以上で入力してください。')
  }
  const stock = Number(form.stockQuantity)
  if (form.stockQuantity.trim() === '' || !Number.isInteger(stock)) {
    errors.push('在庫数は数値で入力してください。')
  } else if (stock < 0) {
    errors.push('在庫数は0以上で入力してください。')
  }
  return errors
}

export default function ProductFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [form, setForm] = useState<FormState>(EMPTY)
  const [errors, setErrors] = useState<string[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (id === undefined) {
      setForm(EMPTY)
      return
    }
    api
      .getProduct(Number(id))
      .then((product) =>
        setForm({
          sku: product.sku,
          name: product.name,
          price: String(product.price),
          stockQuantity: String(product.stockQuantity),
        }),
      )
      .catch((e: unknown) => setError(e instanceof Error ? e.message : String(e)))
  }, [id])

  const update = (key: keyof FormState) => (event: React.ChangeEvent<HTMLInputElement>) =>
    setForm((current) => ({ ...current, [key]: event.target.value }))

  async function submit(event: React.FormEvent) {
    event.preventDefault()
    const validationErrors = validate(form)
    setErrors(validationErrors)
    if (validationErrors.length > 0) {
      return
    }
    const payload = {
      sku: form.sku.trim(),
      name: form.name.trim(),
      price: Number(form.price),
      stockQuantity: Number(form.stockQuantity),
    }
    try {
      if (id === undefined) {
        await api.createProduct(payload)
      } else {
        await api.updateProduct(Number(id), payload)
      }
      navigate('/products')
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e))
    }
  }

  return (
    <section>
      <h2>{id === undefined ? '商品登録' : '商品編集'}</h2>
      <ErrorBanner message={error} />
      {errors.length > 0 && (
        <ul className="error-box" role="alert">
          {errors.map((message) => (
            <li key={message}>{message}</li>
          ))}
        </ul>
      )}

      <form onSubmit={submit} className="form">
        <label>
          SKU
          <input type="text" name="sku" value={form.sku} onChange={update('sku')} />
        </label>
        <label>
          商品名
          <input type="text" name="name" value={form.name} onChange={update('name')} />
        </label>
        <label>
          単価(円)
          <input type="number" name="price" step="0.01" value={form.price} onChange={update('price')} />
        </label>
        <label>
          在庫数
          <input
            type="number"
            name="stockQuantity"
            value={form.stockQuantity}
            onChange={update('stockQuantity')}
          />
        </label>
        <div className="form-actions">
          <button type="submit">保存</button>
          <Link className="button-link" to="/products">
            キャンセル
          </Link>
        </div>
      </form>
    </section>
  )
}
