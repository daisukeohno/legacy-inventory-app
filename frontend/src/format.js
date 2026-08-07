const currencyFormatter = new Intl.NumberFormat('ja-JP');

export function formatYen(amount) {
  if (amount === null || amount === undefined || amount === '') {
    return '';
  }
  return `${currencyFormatter.format(Number(amount))} 円`;
}
