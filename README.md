# Inventory & Order Management (モダナイズ後)

Struts 1 / JSP / 生JDBC / H2 で実装されていた在庫・注文管理システムを、
**Spring Boot 3 (Java 17) + PostgreSQL + React (Vite + TypeScript)** に移行したリポジトリです。

## ディレクトリ構成

```
.
├── backend/                 Spring Boot 3 アプリ (REST API, JPA, Flyway)
│   └── src/main/resources/db/migration/   Flyway マイグレーション (V1..V3)
├── frontend/                React (Vite + TypeScript) SPA + Playwright E2E
└── docker-compose.yml       ローカル開発用 PostgreSQL
```

## 技術スタック

| レイヤ | 移行前 | 移行後 |
|---|---|---|
| 言語 | Java 7 | Java 17 |
| Web | Struts 1.3.10 + JSP | Spring Boot 3 (REST API) |
| データアクセス | 生JDBC (`DbUtil`/`Dao`) | Spring Data JPA |
| スキーマ管理 | `DbInitListener` の DDL ベタ書き | Flyway |
| DB | H2 埋め込み | PostgreSQL (テストは Testcontainers) |
| フロント | JSP + Struts タグ | React + TypeScript |
| ログ | `System.out.println` | SLF4J |

## ローカル起動

```bash
# 1) PostgreSQL 起動
docker compose up -d

# 2) バックエンド (http://localhost:8080)
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 3) フロントエンド (http://localhost:5173)
cd frontend
npm install
npm run dev
```

起動時に Flyway が `V1__init.sql` / `V2__seed.sql` / `V3__normalize_customer.sql` を適用し、
旧 `DbInitListener` 相当のシードデータ (商品6件・得意先2件・注文1件) が投入されます。
`spring.jpa.hibernate.ddl-auto=validate` のため、エンティティとスキーマが不一致だと起動に失敗します。

## REST API

| メソッド | パス | 説明 |
|---|---|---|
| GET | `/api/products?keyword=&lowStockOnly=` | 商品検索 (キーワード / 低在庫フィルタ) |
| POST | `/api/products` | 商品登録 (201) |
| PUT | `/api/products/{id}` | 商品更新 |
| GET | `/api/orders` | 注文一覧 (明細・合計金額付き) |
| POST | `/api/orders` | 注文作成 (201 / 在庫不足時 409) |

エラーは `@RestControllerAdvice` で集中ハンドリングし、
在庫不足 → 409、バリデーション違反 → 400、未検出 → 404 を返します。

## 設定と環境変数

`backend/src/main/resources/application.yml` の値は環境変数で上書きできます。

| 設定 | 環境変数 | デフォルト |
|---|---|---|
| DB URL | `DB_URL` | `jdbc:postgresql://localhost:5432/inventory` |
| DB ユーザー | `DB_USERNAME` | `inventory` |
| DB パスワード | `DB_PASSWORD` | `inventory` |
| 低在庫しきい値 | `APP_LOW_STOCK_THRESHOLD` | `10` |
| CORS 許可オリジン | `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:5173` |

CORS は `app.cors.allowed-origins` に列挙したオリジンのみ許可します (API は外部公開しない前提)。
フロントの API 参照先は `frontend/.env` の `VITE_API_BASE_URL` で変更できます。

## 業務仕様のポイント

- **金額**: すべて `BigDecimal` のスケール0 (円・整数)、丸めは `RoundingMode.HALF_UP`。
  DB カラムは `NUMERIC(19,0)`。
- **在庫引き落とし**: 悲観/楽観ロックを使わず、
  `UPDATE product SET stock_quantity = stock_quantity - :qty WHERE id = :id AND stock_quantity >= :qty`
  のアトミック UPDATE。更新件数 0 件を在庫不足とみなす。
- **トランザクション境界 (意図した仕様変更)**: 注文作成は単一 `@Transactional`。
  1 明細でも在庫不足なら例外を投げ、**注文全体をロールバック**する。
  旧実装は在庫引き落としと注文保存が別トランザクションで、部分的に成功し得た。
- **低在庫しきい値**: `app.low-stock-threshold` (デフォルト 10) をグローバル設定として外出し。
  判定は Service 層で行う。**将来拡張**: `Product` に固有しきい値カラムを追加し、
  値が設定されていればグローバル設定を上書きする形に拡張できる (現状はスコープ外)。
- **得意先の正規化**: 旧 `orders.customer_name` (自由記述) を `customer` テーブルへ正規化し、
  `orders.customer_id` の外部キー参照に変更 (`V3__normalize_customer.sql`)。
  `customer_name` 列は監査・突合用に当面残置しており、削除は将来のマイグレーションで行う。

## テスト

```bash
cd backend && mvn clean verify      # 単体 + Testcontainers(PostgreSQL) 統合テスト (要 Docker)
cd frontend && npm run build        # 型チェック + ビルド
cd frontend && npx playwright install chromium && npx playwright test  # E2E (バックエンド起動が必要)
```

E2E は商品検索 / 低在庫フィルタ / 商品登録 / 正常注文 / 在庫不足時の全ロールバックを画面経由で検証します。

## スコープ外

認証・認可は本リポジトリのスコープ外です。API は外部公開しない前提で、CORS のみで保護しています。
