const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export type Product = {
  id: number
  sku: string
  name: string
  price: number
  stockQuantity: number
  lowStock: boolean
}

export type ProductInput = {
  sku: string
  name: string
  price: number
  stockQuantity: number
}

export type OrderItem = {
  productId: number
  productName: string
  unitPrice: number
  quantity: number
  subtotal: number
}

export type Order = {
  id: number
  customerName: string
  orderDate: string
  status: string
  items: OrderItem[]
  totalAmount: number
}

export type CreateOrderRequest = {
  customerName: string
  items: { productId: number; quantity: number }[]
}

export type FieldError = { field: string; message: string }

export class ApiError extends Error {
  readonly fieldErrors: FieldError[]

  constructor(message: string, fieldErrors: FieldError[] = []) {
    super(message)
    this.name = 'ApiError'
    this.fieldErrors = fieldErrors
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...(init?.headers ?? {}) },
  })

  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new ApiError(body?.message ?? `リクエストに失敗しました (HTTP ${response.status})`,
      body?.fieldErrors ?? [])
  }

  return (await response.json()) as T
}

export function fetchProducts(keyword: string, lowStockOnly: boolean): Promise<Product[]> {
  const params = new URLSearchParams()
  if (keyword.trim()) {
    params.set('keyword', keyword.trim())
  }
  if (lowStockOnly) {
    params.set('lowStockOnly', 'true')
  }
  const query = params.toString()
  return request<Product[]>(`/api/products${query ? `?${query}` : ''}`)
}

export function fetchProduct(id: number): Promise<Product> {
  return request<Product>(`/api/products/${id}`)
}

export function createProduct(product: ProductInput): Promise<Product> {
  return request<Product>('/api/products', { method: 'POST', body: JSON.stringify(product) })
}

export function updateProduct(id: number, product: ProductInput): Promise<Product> {
  return request<Product>(`/api/products/${id}`, { method: 'PUT', body: JSON.stringify(product) })
}

export function fetchOrders(): Promise<Order[]> {
  return request<Order[]>('/api/orders')
}

export function createOrder(order: CreateOrderRequest): Promise<Order> {
  return request<Order>('/api/orders', { method: 'POST', body: JSON.stringify(order) })
}
