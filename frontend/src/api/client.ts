import type {
  ApiErrorBody,
  CreateOrderRequest,
  Order,
  Product,
  ProductRequest,
} from './types'

export class ApiError extends Error {
  readonly status: number
  readonly body?: ApiErrorBody

  constructor(status: number, message: string, body?: ApiErrorBody) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.body = body
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`/api${path}`, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...(init?.headers ?? {}) },
  })

  if (!response.ok) {
    let body: ApiErrorBody | undefined
    try {
      body = (await response.json()) as ApiErrorBody
    } catch {
      body = undefined
    }
    throw new ApiError(
      response.status,
      body?.message ?? `リクエストが失敗しました(HTTP ${response.status})。`,
      body,
    )
  }

  if (response.status === 204) {
    return undefined as T
  }
  return (await response.json()) as T
}

export const api = {
  listProducts(keyword: string, lowStockOnly: boolean): Promise<Product[]> {
    const params = new URLSearchParams()
    if (keyword.trim() !== '') {
      params.set('keyword', keyword.trim())
    }
    if (lowStockOnly) {
      params.set('lowStockOnly', 'true')
    }
    const query = params.toString()
    return request<Product[]>(`/products${query === '' ? '' : `?${query}`}`)
  },

  getProduct(id: number): Promise<Product> {
    return request<Product>(`/products/${id}`)
  },

  createProduct(payload: ProductRequest): Promise<Product> {
    return request<Product>('/products', { method: 'POST', body: JSON.stringify(payload) })
  },

  updateProduct(id: number, payload: ProductRequest): Promise<Product> {
    return request<Product>(`/products/${id}`, { method: 'PUT', body: JSON.stringify(payload) })
  },

  listOrders(): Promise<Order[]> {
    return request<Order[]>('/orders')
  },

  createOrder(payload: CreateOrderRequest): Promise<Order> {
    return request<Order>('/orders', { method: 'POST', body: JSON.stringify(payload) })
  },
}
