import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ApiRequestError, createProduct, fetchProduct, updateProduct } from '../api/client'

type FormState = {
  sku: string
  name: string
  price: string
  stockQuantity: string
}

const emptyForm: FormState = { sku: '', name: '', price: '', stockQuantity: '' }

/** 旧 ProductForm.validate と同じ条件をクライアント側でも実施する。 */
function validate(form: FormState): Record<string, string> {
  const errors: Record<string, string> = {}
  if (form.sku.trim() === '') {
    errors.sku = 'SKUを入力してください。'
  }
  if (form.name.trim() === '') {
    errors.name = '商品名を入力してください。'
  }
  const price = Number(form.price)
  if (form.price.trim() === '' || Number.isNaN(price)) {
    errors.price = '価格は数値で入力してください。'
  } else if (price < 0) {
    errors.price = '価格は0以上で入力してください。'
  }
  const stock = Number(form.stockQuantity)
  if (form.stockQuantity.trim() === '' || !Number.isInteger(stock)) {
    errors.stockQuantity = '在庫数は整数で入力してください。'
  } else if (stock < 0) {
    errors.stockQuantity = '在庫数は0以上で入力してください。'
  }
  return errors
}

export default function ProductFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const productId = id === undefined ? null : Number(id)

  const [form, setForm] = useState<FormState>(emptyForm)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [message, setMessage] = useState<string | null>(null)

  useEffect(() => {
    if (productId === null) {
      setForm(emptyForm)
      return
    }
    void (async () => {
      try {
        const product = await fetchProduct(productId)
        setForm({
          sku: product.sku,
          name: product.name,
          price: String(product.price),
          stockQuantity: String(product.stockQuantity),
        })
      } catch (e) {
        setMessage(e instanceof Error ? e.message : '商品の取得に失敗しました。')
      }
    })()
  }, [productId])

  const update = (field: keyof FormState) => (value: string) => {
    setForm((current) => ({ ...current, [field]: value }))
    // 修正した項目のエラー表示は即時に取り下げる。
    setErrors((current) => {
      if (current[field] === undefined) {
        return current
      }
      const next = { ...current }
      delete next[field]
      return next
    })
  }

  const submit = async (event: React.FormEvent) => {
    event.preventDefault()
    const validationErrors = validate(form)
    setErrors(validationErrors)
    if (Object.keys(validationErrors).length > 0) {
      return
    }

    const body = {
      sku: form.sku.trim(),
      name: form.name.trim(),
      price: Number(form.price),
      stockQuantity: Number(form.stockQuantity),
    }

    try {
      if (productId === null) {
        await createProduct(body)
      } else {
        await updateProduct(productId, body)
      }
      navigate('/products')
    } catch (e) {
      if (e instanceof ApiRequestError) {
        setErrors(e.fieldErrors)
        setMessage(e.message)
      } else {
        setMessage('保存に失敗しました。')
      }
    }
  }

  return (
    <section>
      <h2>{productId === null ? '商品登録' : '商品編集'}</h2>
      {message !== null && <p className="error">{message}</p>}
      <form className="stacked-form" onSubmit={submit} noValidate>
        <label>
          SKU
          <input value={form.sku} onChange={(e) => update('sku')(e.target.value)} />
          {errors.sku !== undefined && <span className="field-error">{errors.sku}</span>}
        </label>
        <label>
          商品名
          <input value={form.name} onChange={(e) => update('name')(e.target.value)} />
          {errors.name !== undefined && <span className="field-error">{errors.name}</span>}
        </label>
        <label>
          価格
          <input value={form.price} onChange={(e) => update('price')(e.target.value)} />
          {errors.price !== undefined && <span className="field-error">{errors.price}</span>}
        </label>
        <label>
          在庫数
          <input
            value={form.stockQuantity}
            onChange={(e) => update('stockQuantity')(e.target.value)}
          />
          {errors.stockQuantity !== undefined && (
            <span className="field-error">{errors.stockQuantity}</span>
          )}
        </label>
        <div className="actions">
          <button type="submit">保存</button>
          <button type="button" className="secondary" onClick={() => navigate('/products')}>
            キャンセル
          </button>
        </div>
      </form>
    </section>
  )
}
