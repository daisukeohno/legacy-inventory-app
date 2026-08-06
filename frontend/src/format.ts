const yenFormatter = new Intl.NumberFormat('ja-JP', {
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
});

const quantityFormatter = new Intl.NumberFormat('ja-JP');

/** 金額は必ず3桁区切り + 「円」表記で表示する (例: 128,000 円)。 */
export function formatYen(amount: number): string {
  return `${yenFormatter.format(amount)} 円`;
}

export function formatQuantity(quantity: number): string {
  return quantityFormatter.format(quantity);
}

export function formatDate(isoDate: string): string {
  const [year, month, day] = isoDate.split('-');
  return `${year}/${month}/${day}`;
}
