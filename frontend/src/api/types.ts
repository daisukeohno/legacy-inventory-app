export interface Product {
  id: number
  sku: string
  name: string
  price: number
  stockQuantity: number
  lowStock: boolean
}

export interface ProductRequest {
  sku: string
  name: string
  price: number
  stockQuantity: number
}

export interface OrderItem {
  id: number
  productId: number
  productName: string
  unitPrice: number
  quantity: number
  subtotal: number
}

export interface Order {
  id: number
  customerName: string
  orderDate: string
  status: string
  items: OrderItem[]
  totalAmount: number
}

export interface CreateOrderRequest {
  customerName: string
  items: { productId: number; quantity: number }[]
}

export interface FieldError {
  field: string
  message: string
}

export interface ApiErrorBody {
  status: number
  error: string
  message: string
  fieldErrors: FieldError[]
}
