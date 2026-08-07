import type { Customer, Order, OrderInput, Product, ProductInput } from './types'

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export class ApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.status = status
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...(init?.headers ?? {}) },
  })
  if (!response.ok) {
    let message = `リクエストに失敗しました (HTTP ${response.status})`
    try {
      const body = await response.json()
      if (typeof body?.message === 'string') {
        const details: string[] = Array.isArray(body.details) ? body.details : []
        message = details.length > 0 ? `${body.message} (${details.join(', ')})` : body.message
      }
    } catch {
      // レスポンスがJSONでない場合は既定のメッセージを使う
    }
    throw new ApiError(message, response.status)
  }
  if (response.status === 204) {
    return undefined as T
  }
  return (await response.json()) as T
}

export function fetchProducts(keyword: string, lowStockOnly: boolean): Promise<Product[]> {
  const params = new URLSearchParams()
  if (keyword.trim() !== '') params.set('keyword', keyword.trim())
  if (lowStockOnly) params.set('lowStockOnly', 'true')
  const query = params.toString()
  return request<Product[]>(`/api/products${query === '' ? '' : `?${query}`}`)
}

export function fetchProduct(id: number): Promise<Product> {
  return request<Product>(`/api/products/${id}`)
}

export function createProduct(input: ProductInput): Promise<Product> {
  return request<Product>('/api/products', { method: 'POST', body: JSON.stringify(input) })
}

export function updateProduct(id: number, input: ProductInput): Promise<Product> {
  return request<Product>(`/api/products/${id}`, { method: 'PUT', body: JSON.stringify(input) })
}

export function fetchOrders(): Promise<Order[]> {
  return request<Order[]>('/api/orders')
}

export function createOrder(input: OrderInput): Promise<Order> {
  return request<Order>('/api/orders', { method: 'POST', body: JSON.stringify(input) })
}

export function fetchCustomers(): Promise<Customer[]> {
  return request<Customer[]>('/api/customers')
}
