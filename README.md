# Inventory & Order Management System (Spring Boot 3 + React)

在庫・注文管理システム（架空の商社を想定）。Struts 1.3 + JSP + 生JDBC + H2 の
レガシー実装から、Spring Boot 3（Java 17 / REST API）+ React（Vite + TypeScript）へ
マイグレーションしたモノレポです。**業務要件（在庫チェック・注文明細・合計金額計算）は
変更せずに再現**しています。

## Before / After

| 項目 | Before（移行元） | After（本リポジトリ） |
|---|---|---|
| 言語 | Java 7 | Java 17 |
| Webフレームワーク | Struts 1.3.10（2013年EOL） | Spring Boot 3（REST API） |
| ビュー | JSP + Strutsタグ | React 19 + Vite + TypeScript |
| データアクセス | 生JDBC（`ProductDao` / `OrderDao`） | Spring Data JPA（`ProductRepository` / `OrderRepository`） |
| 業務ロジック | `Action` クラスに直書き | `@Service`（`ProductService` / `OrderService`） |
| トランザクション | コネクション単位でバラバラ | `@Transactional` で注文単位に統一 |
| DB | H2 埋め込み（起動毎にリセット） | PostgreSQL（本番ターゲット）/ H2（テスト） |
| スキーマ管理 | `DbInitListener` にDDLベタ書き | Flyway（`backend/src/main/resources/db/migration`） |
| DB接続情報 | `DbUtil` にハードコード | `application.yml` + 環境変数（`DB_URL` / `DB_USER` / `DB_PASSWORD`）、HikariCP |
| 金額型 | `double` | `BigDecimal`（scale=0 / HALF_UP、DBは `NUMERIC(19,0)`） |
| 注文日 / 状態 | `String` / `String` | `LocalDate` / enum `OrderStatus{NEW, SHIPPED}` |
| 得意先 | `orders.customer_name` の自由記述 | `customer` エンティティ + FK（`orders.customer_id`） |
| 入力検証 | `ActionForm.validate()` | Bean Validation（`@NotBlank` / `@PositiveOrZero` 等） |
| 例外処理 | `catch(SQLException){ System.out.println(...) }` | `@RestControllerAdvice`（409/404/400）+ SLF4J |
| 低在庫しきい値 | `Product.isLowStock()` に `10` 直書き | `app.inventory.low-stock-threshold`（既定10）を Service で判定 |
| 金額表示 | `128000.0 円` | `128,000 円`（`Intl.NumberFormat('ja-JP')`）+ 在庫少バッジ |
| テスト | なし | JUnit 5（Service / Repository / Controller）+ Testcontainers + Playwright E2E |

### 意図的な挙動変更（リグレッションではない）

- **注文は全ロールバック**: 旧実装は在庫不足の直前までの商品の在庫を引き落としたまま
  エラー画面を返していた。新実装は `@Transactional` により1商品でも在庫不足なら
  注文全体を失敗させ、引き落とし済みの在庫も戻す（HTTP 409）。
- **注文明細の送信形式**: 旧 `productIds[]` / `quantities[]` の並行配列から、
  `items: [{ productId, quantity }]` のオブジェクト配列へ再設計（明細ずれの防止）。

## リポジトリ構成

```
backend/    Spring Boot 3 REST API（Java 17 / Maven）
frontend/   React + Vite + TypeScript（4画面）+ Playwright E2E
docker-compose.yml  ローカル開発用 PostgreSQL
```

## ローカル起動

```bash
# 1) PostgreSQL 起動（初回のみイメージ取得）
docker compose up -d

# 2) バックエンド（http://localhost:8080）
cd backend
mvn spring-boot:run
#   起動時に Flyway がスキーマ作成 + シードデータ投入 + customer名寄せ移行を実行

# 3) フロントエンド（http://localhost:5173）
cd frontend
npm install
cp .env.example .env    # VITE_API_BASE_URL を必要に応じて変更
npm run dev
```

DB接続先は環境変数で上書きできます。

```bash
DB_URL=jdbc:postgresql://db.example:5432/inventory DB_USER=app DB_PASSWORD=*** mvn spring-boot:run
```

## REST API

| メソッド | パス | 説明 |
|---|---|---|
| GET | `/api/products?keyword=&lowStockOnly=` | 商品検索（name/sku 部分一致・大文字小文字無視、低在庫フィルタ） |
| GET | `/api/products/{id}` | 商品取得 |
| POST | `/api/products` | 商品登録（201） |
| PUT | `/api/products/{id}` | 商品更新 |
| GET | `/api/customers` | 得意先一覧 |
| GET | `/api/orders` | 注文一覧（明細・合計金額付き） |
| POST | `/api/orders` | 注文確定（201 / 在庫不足は409） |

エラーレスポンスは `{ timestamp, status, message, details[] }` 形式。
バリデーション違反=400、リソース未存在=404、在庫不足=409。

## 設定

`backend/src/main/resources/application.yml`

| キー | 既定値 | 説明 |
|---|---|---|
| `app.inventory.low-stock-threshold` | `10` | 低在庫と判定する在庫数のしきい値 |
| `app.cors.allowed-origins` | `http://localhost:5173` | CORS 許可オリジン（APIは外部非公開前提） |
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | localhost の docker-compose 値 | DB接続情報（環境変数） |

## データ移行（customer 名寄せ）

`V3__customer_fk_migration.sql` で、旧 `orders.customer_name`（自由記述）を
`customer` マスタへ名寄せします。

1. `orders.customer_id` を nullable で追加
2. `customer.name` との完全一致でバックフィル
3. 未一致の得意先名は `customer` へ新規登録して紐付け
4. `customer_id` を NOT NULL 化 + FK制約 `orders_customer_fk` 追加
5. `customer_name` は互換のため非正規化コピーとして残置（将来削除）

> 表記ゆれ（全角/半角・空白）があると完全一致に失敗し、別得意先として新規作成されます。
> 本番データ移行時は事前にデータ確認を推奨します。

## テスト

```bash
cd backend && mvn test          # 単体 + Repository + Controller + Testcontainers結合
cd frontend && npm run lint     # 型チェック
cd frontend && npm run build
cd frontend && npx playwright test   # E2E（backend/frontend 起動が前提）
```

- 結合テストは Testcontainers の PostgreSQL に対して Flyway マイグレーション、
  アトミック在庫UPDATE、在庫不足時の全ロールバックを検証します（Docker必須。
  Docker が無い環境では自動スキップ）。

## 将来拡張の余地（今回スコープ外）

- 低在庫しきい値の商品ごと可変化（`product.low_stock_threshold` 列を追加し、
  未設定時はグローバル設定へフォールバック）。判定は `ProductService.isLowStock()` に
  集約済みのため拡張が容易。
- 認証・認可は本リポジトリのスコープ外（APIは外部非公開前提）。
