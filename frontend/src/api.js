const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

async function request(path, options = {}) {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });

  if (!response.ok) {
    let detail = `リクエストに失敗しました (${response.status})`;
    try {
      const problem = await response.json();
      if (problem.detail) {
        detail = problem.detail;
      }
      if (problem.errors) {
        detail = Object.entries(problem.errors)
          .map(([field, message]) => `${field}: ${message}`)
          .join(' / ');
      }
      if (response.status === 409 && problem.productName) {
        detail = `「${problem.productName}」の在庫が不足しています。`;
      }
    } catch {
      // response body was not a ProblemDetail
    }
    throw new Error(detail);
  }

  if (response.status === 204) {
    return null;
  }
  return response.json();
}

export function fetchProducts({ keyword = '', lowStock = false } = {}) {
  const params = new URLSearchParams();
  if (keyword) params.set('keyword', keyword);
  if (lowStock) params.set('lowStock', 'true');
  const query = params.toString();
  return request(`/api/products${query ? `?${query}` : ''}`);
}

export function fetchProduct(id) {
  return request(`/api/products/${id}`);
}

export function createProduct(body) {
  return request('/api/products', { method: 'POST', body: JSON.stringify(body) });
}

export function updateProduct(id, body) {
  return request(`/api/products/${id}`, { method: 'PUT', body: JSON.stringify(body) });
}

export function fetchOrders() {
  return request('/api/orders');
}

export function createOrder(body) {
  return request('/api/orders', { method: 'POST', body: JSON.stringify(body) });
}
