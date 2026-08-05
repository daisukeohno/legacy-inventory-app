const yenFormatter = new Intl.NumberFormat('ja-JP');

/** 円単位の整数金額を「1,234 円」形式に整形する。 */
export function formatYen(amount: number): string {
  return `${yenFormatter.format(amount)} 円`;
}
