import { useEffect, useState } from 'react'
import { ApiError, createProduct, fetchProduct, updateProduct, type FieldError } from '../api'

type Props = {
  productId?: number
  onDone: () => void
}

type FormState = {
  sku: string
  name: string
  price: string
  stockQuantity: string
}

const EMPTY_FORM: FormState = { sku: '', name: '', price: '', stockQuantity: '' }

export function ProductFormPage({ productId, onDone }: Props) {
  const [form, setForm] = useState<FormState>(EMPTY_FORM)
  const [fieldErrors, setFieldErrors] = useState<FieldError[]>([])
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (productId === undefined) {
      setForm(EMPTY_FORM)
      return
    }
    fetchProduct(productId)
      .then((product) =>
        setForm({
          sku: product.sku,
          name: product.name,
          price: String(product.price),
          stockQuantity: String(product.stockQuantity),
        }),
      )
      .catch((e: unknown) =>
        setError(e instanceof Error ? e.message : '商品の取得に失敗しました。'),
      )
  }, [productId])

  const errorFor = (field: string) => fieldErrors.find((it) => it.field === field)?.message

  const validate = (): FieldError[] => {
    const errors: FieldError[] = []
    if (!form.sku.trim()) {
      errors.push({ field: 'sku', message: 'SKUを入力してください。' })
    }
    if (!form.name.trim()) {
      errors.push({ field: 'name', message: '商品名を入力してください。' })
    }
    if (!form.price.trim()) {
      errors.push({ field: 'price', message: '単価を入力してください。' })
    } else if (Number.isNaN(Number(form.price))) {
      errors.push({ field: 'price', message: '単価は数値で入力してください。' })
    } else if (Number(form.price) < 0) {
      errors.push({ field: 'price', message: '単価は0以上で入力してください。' })
    }
    if (!Number.isInteger(Number(form.stockQuantity)) || form.stockQuantity.trim() === '') {
      errors.push({ field: 'stockQuantity', message: '在庫数は数値で入力してください。' })
    } else if (Number(form.stockQuantity) < 0) {
      errors.push({ field: 'stockQuantity', message: '在庫数は0以上で入力してください。' })
    }
    return errors
  }

  const submit = async (event: React.FormEvent) => {
    event.preventDefault()
    const errors = validate()
    setFieldErrors(errors)
    if (errors.length > 0) {
      return
    }

    const payload = {
      sku: form.sku.trim(),
      name: form.name.trim(),
      price: Number(form.price),
      stockQuantity: Number(form.stockQuantity),
    }

    setSaving(true)
    try {
      if (productId === undefined) {
        await createProduct(payload)
      } else {
        await updateProduct(productId, payload)
      }
      onDone()
    } catch (e) {
      if (e instanceof ApiError) {
        setFieldErrors(e.fieldErrors)
        setError(e.message)
      } else {
        setError('保存に失敗しました。')
      }
    } finally {
      setSaving(false)
    }
  }

  return (
    <section>
      <h2>{productId === undefined ? '新規商品登録' : '商品編集'}</h2>

      {error && <p className="error-box">{error}</p>}

      <form className="form" onSubmit={submit} noValidate>
        <label>
          SKU
          <input
            type="text"
            value={form.sku}
            onChange={(event) => setForm({ ...form, sku: event.target.value })}
          />
          {errorFor('sku') && <span className="field-error">{errorFor('sku')}</span>}
        </label>
        <label>
          商品名
          <input
            type="text"
            value={form.name}
            onChange={(event) => setForm({ ...form, name: event.target.value })}
          />
          {errorFor('name') && <span className="field-error">{errorFor('name')}</span>}
        </label>
        <label>
          単価 (円)
          <input
            type="text"
            inputMode="decimal"
            value={form.price}
            onChange={(event) => setForm({ ...form, price: event.target.value })}
          />
          {errorFor('price') && <span className="field-error">{errorFor('price')}</span>}
        </label>
        <label>
          在庫数
          <input
            type="text"
            inputMode="numeric"
            value={form.stockQuantity}
            onChange={(event) => setForm({ ...form, stockQuantity: event.target.value })}
          />
          {errorFor('stockQuantity') && (
            <span className="field-error">{errorFor('stockQuantity')}</span>
          )}
        </label>

        <div className="form-actions">
          <button type="submit" className="primary" disabled={saving}>
            保存
          </button>
          <button type="button" className="link" onClick={onDone}>
            キャンセル
          </button>
        </div>
      </form>
    </section>
  )
}
