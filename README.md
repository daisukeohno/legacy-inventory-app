# Inventory & Order Management System (Spring Boot 3 + React)

在庫・注文管理アプリケーション。もともと Struts 1 + JSP + 生JDBC（Java 7 / H2）で書かれていた
レガシー実装を、**Spring Boot 3（Java 17, REST API）+ React（Vite + TypeScript）+ PostgreSQL**
のモノレポ構成へ移行したものです。業務ロジック（低在庫判定・小計/合計計算・在庫チェック付き
引き落とし・検索条件・バリデーション）は旧実装の挙動をそのまま踏襲しています。

## 構成

```
backend/    Spring Boot 3 / Java 17 / Spring Data JPA / Flyway / PostgreSQL
frontend/   React 19 / TypeScript / Vite / React Router / Playwright(E2E)
docker-compose.yml   ローカル開発用 PostgreSQL
```

| 旧 | 新 |
|---|---|
| Struts `Action` / `Form` | `@RestController` + リクエスト/レスポンスDTO |
| 生JDBC `Dao` | Spring Data JPA `Repository` |
| Action内の業務ロジック | `ProductService` / `OrderService`（`@Transactional`） |
| `DbUtil` のハードコード接続 | `application.yml` + 環境変数 |
| `DbInitListener` のDDL直書き | Flyway マイグレーション（`V1__init.sql`, `V2__seed.sql`） |
| `System.out.println` での例外握りつぶし | SLF4J + `@RestControllerAdvice`（400 / 409 / 500） |
| JSP + Strutsタグ | React コンポーネント（4画面） |

## 前提

- JDK 17
- Maven 3.6+
- Node.js 22（`frontend/.nvmrc`。Vite 8 のため 20.19+ / 22+ が必要）
- Docker（ローカルPostgreSQL用）

## 1. PostgreSQL を起動する

```bash
docker compose up -d
```

`inventory` データベース（ユーザー/パスワード: `inventory` / `inventory`、ポート 5432）が起動します。
初期化からやり直す場合は `docker compose down -v`。

## 2. バックエンドを起動する

```bash
cd backend
mvn spring-boot:run     # http://localhost:8080
```

起動時に Flyway が `backend/src/main/resources/db/migration` のマイグレーションを適用し、
テーブル作成とシードデータ投入を行います（Hibernate は `ddl-auto: validate` でスキーマ検証のみ）。

### 環境変数

| 変数 | デフォルト | 用途 |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/inventory` | 接続URL |
| `SPRING_DATASOURCE_USERNAME` | `inventory` | ユーザー |
| `SPRING_DATASOURCE_PASSWORD` | `inventory` | パスワード |
| `SERVER_PORT` | `8080` | 待受ポート |
| `INVENTORY_LOW_STOCK_THRESHOLD` | `10` | 低在庫と判定する在庫数（未満で低在庫） |
| `INVENTORY_CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:4173` | CORS許可オリジン |

### REST API

| メソッド | パス | 説明 |
|---|---|---|
| GET | `/api/products?keyword=&lowStockOnly=` | 商品一覧・検索（name/sku 部分一致、大文字小文字無視） |
| GET | `/api/products/{id}` | 商品取得 |
| POST | `/api/products` | 商品登録 |
| PUT | `/api/products/{id}` | 商品更新 |
| GET | `/api/orders` | 注文一覧（明細・合計込み） |
| POST | `/api/orders` | 新規注文（在庫チェック→引き落とし→保存を単一トランザクション） |

バリデーションエラーは 400、在庫不足は 409（`InsufficientStockException`）、想定外は 500 を返します。

## 3. フロントエンドを起動する

```bash
cd frontend
nvm use          # Node 22
npm install
npm run dev      # http://localhost:5173
```

開発サーバーは `/api` を `BACKEND_URL`（デフォルト `http://localhost:8080`）へプロキシします。
別オリジンのAPIを直接叩く場合は `VITE_API_BASE_URL` を設定してください（`frontend/.env.example` 参照）。

画面: 商品一覧（キーワード検索・低在庫フィルタ）／商品登録・編集／注文一覧（明細・合計）／新規注文。
金額は3桁区切り + 円表記、低在庫はバッジで強調表示します。

## テスト

```bash
# バックエンド: 単体テスト(JUnit 5 + Mockito) + Testcontainers PostgreSQL の結合テスト
cd backend && mvn test        # Docker が必要

# フロントエンド: 型チェック / Lint / ビルド
cd frontend && npm run typecheck && npm run lint && npm run build

# E2E (Playwright): PostgreSQL + バックエンドを起動した状態で実行する
cd frontend
npx playwright install --with-deps chromium   # 初回のみ
npm run build && npm run e2e                  # プレビューサーバー(4173)は自動起動
```

E2E は商品検索・低在庫フィルタ・商品登録（バリデーション含む）・注文一覧の合計表示・
新規注文での在庫引き落とし・在庫不足時のエラーをカバーしています。

### CI

`.github/workflows/ci.yml` が push（master）と Pull Request で以下を実行します。

- `backend`: `mvn test`（Testcontainers PostgreSQL 含む）
- `frontend`: `npm run typecheck` / `npm run lint` / `npm run build`
- `e2e`: PostgreSQL サービスコンテナ + バックエンド jar 起動 + Playwright

## 業務ロジック（旧実装からの踏襲）

- 低在庫: `stockQuantity < 10`（しきい値は設定で変更可能）
- 小計: `unitPrice * quantity`、合計: 明細小計の総和（いずれも `BigDecimal`、`setScale(0, HALF_UP)`）
- 在庫引き落とし: `stock_quantity >= quantity` の条件付きUPDATE。0件更新なら在庫不足エラーでロールバック
- 検索: キーワードが name または sku に部分一致（大文字小文字無視）AND 低在庫フィルタ
- 商品バリデーション: sku必須・name必須・price非負・stock非負（整数）
- 注文バリデーション: 得意先名必須、数量1以上の明細が最低1件

## 既知の残課題

- `orders.order_date` は旧実装の挙動維持のため `VARCHAR(20)`（`yyyy-MM-dd` 文字列）。将来 `DATE` 化する。
- 認証・認可は対象外。
