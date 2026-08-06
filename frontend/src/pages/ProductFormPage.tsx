import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ApiRequestError, createProduct, fetchProduct, updateProduct } from '../api';

type FormState = {
  sku: string;
  name: string;
  price: string;
  stockQuantity: string;
};

const EMPTY_FORM: FormState = { sku: '', name: '', price: '', stockQuantity: '' };

export function ProductFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const productId = id ? Number(id) : null;

  const [form, setForm] = useState<FormState>(EMPTY_FORM);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (productId === null) {
      setForm(EMPTY_FORM);
      return;
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
      .catch((e: unknown) => setError(e instanceof Error ? e.message : '商品の取得に失敗しました。'));
  }, [productId]);

  /** 旧 ProductForm.validate() と同じ検証内容をクライアント側でも実施する。 */
  function validate(): Record<string, string> {
    const errors: Record<string, string> = {};
    if (!form.sku.trim()) {
      errors.sku = 'SKUを入力してください。';
    }
    if (!form.name.trim()) {
      errors.name = '商品名を入力してください。';
    }
    const price = Number(form.price);
    if (form.price.trim() === '' || Number.isNaN(price)) {
      errors.price = '単価は数値で入力してください。';
    } else if (price < 0) {
      errors.price = '単価は0以上で入力してください。';
    }
    const stock = Number(form.stockQuantity);
    if (form.stockQuantity.trim() === '' || !Number.isInteger(stock)) {
      errors.stockQuantity = '在庫数は整数で入力してください。';
    } else if (stock < 0) {
      errors.stockQuantity = '在庫数は0以上で入力してください。';
    }
    return errors;
  }

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setError(null);
    const errors = validate();
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) {
      return;
    }

    const payload = {
      sku: form.sku.trim(),
      name: form.name.trim(),
      price: Number(form.price),
      stockQuantity: Number(form.stockQuantity),
    };

    setSaving(true);
    try {
      if (productId === null) {
        await createProduct(payload);
      } else {
        await updateProduct(productId, payload);
      }
      navigate('/products');
    } catch (e) {
      if (e instanceof ApiRequestError) {
        setFieldErrors(e.detail?.fieldErrors ?? {});
        setError(e.message);
      } else {
        setError('商品の保存に失敗しました。');
      }
    } finally {
      setSaving(false);
    }
  }

  return (
    <section>
      <h2>{productId === null ? '商品登録' : '商品編集'}</h2>
      {error && <p className="error" role="alert">{error}</p>}
      <form className="card" onSubmit={handleSubmit}>
        <div className="field">
          <label htmlFor="sku">SKU</label>
          <input
            id="sku"
            type="text"
            value={form.sku}
            onChange={(event) => setForm({ ...form, sku: event.target.value })}
          />
          {fieldErrors.sku && <span className="field-error">{fieldErrors.sku}</span>}
        </div>
        <div className="field">
          <label htmlFor="name">商品名</label>
          <input
            id="name"
            type="text"
            value={form.name}
            onChange={(event) => setForm({ ...form, name: event.target.value })}
          />
          {fieldErrors.name && <span className="field-error">{fieldErrors.name}</span>}
        </div>
        <div className="field">
          <label htmlFor="price">単価(円)</label>
          <input
            id="price"
            type="number"
            step="0.01"
            value={form.price}
            onChange={(event) => setForm({ ...form, price: event.target.value })}
          />
          {fieldErrors.price && <span className="field-error">{fieldErrors.price}</span>}
        </div>
        <div className="field">
          <label htmlFor="stockQuantity">在庫数</label>
          <input
            id="stockQuantity"
            type="number"
            value={form.stockQuantity}
            onChange={(event) => setForm({ ...form, stockQuantity: event.target.value })}
          />
          {fieldErrors.stockQuantity && <span className="field-error">{fieldErrors.stockQuantity}</span>}
        </div>
        <div className="actions">
          <button type="submit" disabled={saving}>保存</button>
          <button type="button" className="secondary" onClick={() => navigate('/products')}>
            キャンセル
          </button>
        </div>
      </form>
    </section>
  );
}
