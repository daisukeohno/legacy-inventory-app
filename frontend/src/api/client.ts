import type {
  ApiError,
  Order,
  OrderCreateRequest,
  Product,
  ProductRequest,
} from './types'

const baseUrl = import.meta.env.VITE_API_BASE_URL ?? ''

export class ApiRequestError extends Error {
  readonly status: number
  readonly fieldErrors: Record<string, string>

  constructor(status: number, message: string, fieldErrors: Record<string, string>) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${baseUrl}${path}`, {
    headers: init?.body ? { 'Content-Type': 'application/json' } : undefined,
    ...init,
  })

  if (!response.ok) {
    let error: ApiError | null = null
    try {
      error = (await response.json()) as ApiError
    } catch {
      error = null
    }
    throw new ApiRequestError(
      response.status,
      error?.message ?? `APIエラーが発生しました (HTTP ${response.status})`,
      error?.fieldErrors ?? {},
    )
  }

  return (await response.json()) as T
}

export function fetchProducts(keyword: string, lowStockOnly: boolean): Promise<Product[]> {
  const params = new URLSearchParams()
  if (keyword.trim() !== '') {
    params.set('keyword', keyword.trim())
  }
  if (lowStockOnly) {
    params.set('lowStockOnly', 'true')
  }
  const query = params.toString()
  return request<Product[]>(`/api/products${query === '' ? '' : `?${query}`}`)
}

export function fetchProduct(id: number): Promise<Product> {
  return request<Product>(`/api/products/${id}`)
}

export function createProduct(body: ProductRequest): Promise<Product> {
  return request<Product>('/api/products', { method: 'POST', body: JSON.stringify(body) })
}

export function updateProduct(id: number, body: ProductRequest): Promise<Product> {
  return request<Product>(`/api/products/${id}`, { method: 'PUT', body: JSON.stringify(body) })
}

export function fetchOrders(): Promise<Order[]> {
  return request<Order[]>('/api/orders')
}

export function createOrder(body: OrderCreateRequest): Promise<Order> {
  return request<Order>('/api/orders', { method: 'POST', body: JSON.stringify(body) })
}
