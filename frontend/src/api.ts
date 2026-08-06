import type { ApiError, Order, OrderRequest, Product, ProductRequest } from './types';

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

export class ApiRequestError extends Error {
  readonly detail: ApiError | null;

  constructor(message: string, detail: ApiError | null) {
    super(message);
    this.name = 'ApiRequestError';
    this.detail = detail;
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...init,
  });

  if (!response.ok) {
    let detail: ApiError | null = null;
    try {
      detail = (await response.json()) as ApiError;
    } catch {
      detail = null;
    }
    throw new ApiRequestError(detail?.message ?? `通信エラーが発生しました (HTTP ${response.status})`, detail);
  }

  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}

export function fetchProducts(keyword: string, lowStockOnly: boolean): Promise<Product[]> {
  const params = new URLSearchParams();
  if (keyword.trim()) {
    params.set('keyword', keyword.trim());
  }
  if (lowStockOnly) {
    params.set('lowStockOnly', 'true');
  }
  const query = params.toString();
  return request<Product[]>(`/products${query ? `?${query}` : ''}`);
}

export function fetchProduct(id: number): Promise<Product> {
  return request<Product>(`/products/${id}`);
}

export function createProduct(body: ProductRequest): Promise<Product> {
  return request<Product>('/products', { method: 'POST', body: JSON.stringify(body) });
}

export function updateProduct(id: number, body: ProductRequest): Promise<Product> {
  return request<Product>(`/products/${id}`, { method: 'PUT', body: JSON.stringify(body) });
}

export function fetchOrders(): Promise<Order[]> {
  return request<Order[]>('/orders');
}

export function createOrder(body: OrderRequest): Promise<Order> {
  return request<Order>('/orders', { method: 'POST', body: JSON.stringify(body) });
}
