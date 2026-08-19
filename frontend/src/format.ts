const numberFormatter = new Intl.NumberFormat('ja-JP')

/** 3桁区切り + 円表記（例: 128,000 円）。 */
export function formatYen(amount: number): string {
  return `${numberFormatter.format(amount)} 円`
}

export function formatQuantity(quantity: number): string {
  return numberFormatter.format(quantity)
}
