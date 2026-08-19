const yenFormatter = new Intl.NumberFormat('ja-JP', {
  maximumFractionDigits: 0,
})

/** 金額を3桁区切り + 「円」で表示する(旧JSPの "128000.0 円" のような生値表示を置き換え)。 */
export function formatYen(amount: number): string {
  return `${yenFormatter.format(amount)} 円`
}
