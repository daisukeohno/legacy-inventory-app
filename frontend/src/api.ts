export interface Product {
  id: number;
  sku: string;
  name: string;
  price: number;
  stockQuantity: number;
  lowStock: boolean;
}

export interface ProductRequest {
  sku: string;
  name: string;
  price: number;
  stockQuantity: number;
}

export interface OrderItem {
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
}

export interface Order {
  id: number;
  customerId: number;
  customerName: string;
  orderDate: string;
  status: 'NEW' | 'SHIPPED';
  items: OrderItem[];
  totalAmount: number;
}

export interface OrderRequest {
  customerName: string;
  items: { productId: number; quantity: number }[];
}

interface ApiError {
  status: number;
  message: string;
  details?: string[];
}

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...(init?.headers ?? {}) },
  });
  if (!response.ok) {
    let message = `リクエストに失敗しました (HTTP ${response.status})`;
    try {
      const body = (await response.json()) as ApiError;
      message = body.details?.length ? `${body.message} ${body.details.join(' / ')}` : body.message;
    } catch {
      /* レスポンスがJSONでない場合は既定メッセージを使う */
    }
    throw new Error(message);
  }
  return (await response.json()) as T;
}

export const api = {
  listProducts: (keyword: string, lowStockOnly: boolean) => {
    const params = new URLSearchParams();
    if (keyword) params.set('keyword', keyword);
    if (lowStockOnly) params.set('lowStockOnly', 'true');
    const query = params.toString();
    return request<Product[]>(`/api/products${query ? `?${query}` : ''}`);
  },
  getProduct: (id: number) => request<Product>(`/api/products/${id}`),
  createProduct: (body: ProductRequest) =>
    request<Product>('/api/products', { method: 'POST', body: JSON.stringify(body) }),
  updateProduct: (id: number, body: ProductRequest) =>
    request<Product>(`/api/products/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  listOrders: () => request<Order[]>('/api/orders'),
  createOrder: (body: OrderRequest) =>
    request<Order>('/api/orders', { method: 'POST', body: JSON.stringify(body) }),
};
