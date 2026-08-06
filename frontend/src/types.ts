export type Product = {
  id: number;
  sku: string;
  name: string;
  price: number;
  stockQuantity: number;
  lowStock: boolean;
};

export type ProductRequest = {
  sku: string;
  name: string;
  price: number;
  stockQuantity: number;
};

export type OrderItem = {
  id: number;
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
};

export type Order = {
  id: number;
  customerName: string;
  orderDate: string;
  status: string;
  items: OrderItem[];
  totalAmount: number;
};

export type OrderRequest = {
  customerName: string;
  items: { productId: number; quantity: number }[];
};

export type ApiError = {
  status: number;
  error: string;
  message: string;
  fieldErrors?: Record<string, string>;
};
