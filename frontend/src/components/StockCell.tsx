/** 在庫数の表示。低在庫(既定10未満)はバッジ + 色で強調する。 */
export default function StockCell({
  stockQuantity,
  lowStock,
}: {
  stockQuantity: number
  lowStock: boolean
}) {
  if (!lowStock) {
    return <span data-testid="stock">{stockQuantity}</span>
  }
  return (
    <span className="low-stock">
      <span data-testid="stock">{stockQuantity}</span>
      <span className="low-stock-badge">在庫少</span>
    </span>
  )
}
