export type Product = {
  id: number
  sku: string
  name: string
  price: number
  stockQuantity: number
  lowStock: boolean
}

export type Customer = {
  id: number
  name: string
  email: string | null
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
  customer: Customer
  orderDate: string
  status: 'NEW' | 'SHIPPED'
  items: OrderItem[]
  totalAmount: number
}

export type ProductInput = {
  sku: string
  name: string
  price: number
  stockQuantity: number
}

export type OrderInput = {
  customerId: number
  items: { productId: number; quantity: number }[]
}
