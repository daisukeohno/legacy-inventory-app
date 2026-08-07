const yenFormatter = new Intl.NumberFormat('ja-JP')

export function formatYen(amount: number): string {
  return `${yenFormatter.format(amount)} 円`
}
