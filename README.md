# Inventory & Order Management (Spring Boot 3 + React)

Struts 1.3.10 + JSP + 生JDBC で実装されていた在庫・注文管理アプリを、
Spring Boot 3 (Java 17, REST API) + React にモダナイズしたモノレポです。
旧 Struts/JSP 資産（`src/main/webapp`, 旧 Action/Form/DAO, `DbUtil`, `DbInitListener`, 旧 `pom.xml`）は削除済みです。

## 構成

| ディレクトリ | 内容 |
|---|---|
| `backend/` | Spring Boot 3 / Java 17 / Spring Data JPA / Flyway / PostgreSQL |
| `frontend/` | React 18 + Vite（商品一覧・商品登録/編集・注文一覧・新規注文の4画面） |
| `e2e/` | Playwright による E2E テスト |
| `docker-compose.yml` | PostgreSQL + backend + frontend のローカル起動 |

## 起動方法

```bash
docker compose up --build
```

- フロントエンド: http://localhost:5173
- API: http://localhost:8080/api/products

DB は初回起動時に Flyway マイグレーション（`backend/src/main/resources/db/migration`）でスキーマ作成と
シード投入（旧 `DbInitListener` から移設）を行います。

### 個別に起動する場合

```bash
docker compose up -d db
cd backend && mvn spring-boot:run
cd frontend && npm install && npm run dev
```

## API

| メソッド | パス | 説明 |
|---|---|---|
| GET | `/api/products?keyword=&lowStock=` | 商品一覧（キーワード検索・低在庫フィルタ） |
| GET | `/api/products/{id}` | 商品取得 |
| POST | `/api/products` | 商品登録 |
| PUT | `/api/products/{id}` | 商品更新 |
| GET | `/api/orders` | 注文一覧（明細・合計金額付き） |
| POST | `/api/orders` | 注文作成（在庫チェック＋在庫引き落とし） |

エラーは RFC 7807 `ProblemDetail` で返します（在庫不足=409、バリデーション=400、未検出=404、想定外=500）。

## 設定項目

| 設定 | 既定値 | 説明 |
|---|---|---|
| `inventory.low-stock-threshold` | `10` | 低在庫と判定する在庫数のしきい値（旧 `Product.isLowStock()` の `< 10` を外部化） |
| `app.cors.allowed-origins` / `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | CORS で許可するフロントオリジン（カンマ区切り） |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/inventory` | DB 接続 URL |
| `SPRING_DATASOURCE_USERNAME` | `inventory` | DB ユーザー |
| `SPRING_DATASOURCE_PASSWORD` | `inventory` | DB パスワード |
| `VITE_API_BASE_URL`（frontend） | `http://localhost:8080` | フロントから見た API のベース URL |

## テスト

```bash
cd backend  && mvn verify          # 単体(H2) + Flyway統合(Testcontainers PostgreSQL)
cd frontend && npm test && npm run lint && npm run build
cd e2e      && npm install && npx playwright install --with-deps && npm test   # docker compose 起動中に実行
```

## 移行にあたっての設計方針

- **金額**: `BigDecimal`、スケール0（円・整数）、`RoundingMode.HALF_UP`。DB は `NUMERIC(19,0)`。
- **在庫の並行制御**: ロックを使わず条件付きアトミック UPDATE
  （`UPDATE product SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?`）。
  更新件数が0なら `InsufficientStockException`。旧 `ProductDao.decreaseStock` の意味をそのまま維持。
- **トランザクション**: `OrderService.placeOrder()` を `@Transactional` 化。1商品でも在庫不足なら
  注文全体をロールバック（旧実装は在庫引き落としと注文保存が別トランザクションで、
  片方だけ成功しうる不整合があった。これは意図的な改善）。
- **検索/低在庫フィルタ**: 旧 `ProductDao.search()` の Java 側全件フィルタを DB 側 WHERE 句へ移動。
- **customer**: 旧 `orders.customer_name`（自由記述）を `customer` への外部キー参照に置換。
  `V2__customer_fk_migration.sql` で完全一致による突合 → 未一致は `customer` に新規作成 → NOT NULL + FK 化 →
  `customer_name` 列削除、の順で移行。
- **注文ステータス/日付**: `OrderStatus` enum（`NEW` / `SHIPPED`、`@Enumerated(STRING)`）、`LocalDate`。
- 認証・認可はスコープ外。API は外部公開しない前提。

## 将来の拡張余地

- **商品別の低在庫しきい値**: `product.low_stock_threshold` 列を追加し、NULL のときは
  グローバル設定 `inventory.low-stock-threshold` にフォールバックする形へ拡張可能（今回はスコープ外）。
- **顧客名の表記ゆれ名寄せ**: 現状のデータ移行は完全一致のみ。表記ゆれの統合（正規化・あいまい一致）は未対応。
- **Customer 管理 UI**: 現在は注文作成時に得意先名から自動作成のみ。顧客の一覧・編集画面は未実装。
