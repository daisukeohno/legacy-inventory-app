import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createProduct, fetchProduct, updateProduct } from '../api.js';

const EMPTY = { sku: '', name: '', price: '', stockQuantity: '' };

export default function ProductEditPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!id) {
      setForm(EMPTY);
      return;
    }
    fetchProduct(id)
      .then((product) =>
        setForm({
          sku: product.sku,
          name: product.name,
          price: String(product.price),
          stockQuantity: String(product.stockQuantity),
        }),
      )
      .catch((e) => setError(e.message));
  }, [id]);

  const onChange = (field) => (event) => setForm({ ...form, [field]: event.target.value });

  async function onSubmit(event) {
    event.preventDefault();
    const body = {
      sku: form.sku,
      name: form.name,
      price: form.price === '' ? null : Number(form.price),
      stockQuantity: form.stockQuantity === '' ? null : Number(form.stockQuantity),
    };
    try {
      if (id) {
        await updateProduct(id, body);
      } else {
        await createProduct(body);
      }
      navigate('/products');
    } catch (e) {
      setError(e.message);
    }
  }

  return (
    <section>
      <h2>{id ? '商品編集' : '商品登録'}</h2>
      {error && <p role="alert" className="error">{error}</p>}
      <form className="entity-form" onSubmit={onSubmit}>
        <label>
          SKU
          <input value={form.sku} aria-label="SKU" onChange={onChange('sku')} />
        </label>
        <label>
          商品名
          <input value={form.name} aria-label="商品名" onChange={onChange('name')} />
        </label>
        <label>
          価格
          <input type="number" value={form.price} aria-label="価格" onChange={onChange('price')} />
        </label>
        <label>
          在庫数
          <input
            type="number"
            value={form.stockQuantity}
            aria-label="在庫数"
            onChange={onChange('stockQuantity')}
          />
        </label>
        <button type="submit">保存</button>
      </form>
    </section>
  );
}
