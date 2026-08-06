# Inventory & Order Management System (Devin マイグレーションデモ用)

このリポジトリは、**Devinによるレガシーシステムのマイグレーション**をお客様にデモするための
サンプルアプリケーションです。元は Struts 1 + JSP + 生JDBC (Java 7) で実装されており、
現在は **Spring Boot 3 (Java 17, REST API) + React (Vite/TypeScript)** へモダナイズ済みです。

## Before / After

| 項目 | Before | After |
|---|---|---|
| 言語 | Java 7 | Java 17 |
| Webフレームワーク | Struts 1.3.10 (2013年EOL) | Spring Boot 3 (REST API) |
| ビュー | JSP + Strutsタグ | React 18 + Vite + TypeScript |
| データアクセス | 生JDBC (`DbUtil`/`ProductDao`/`OrderDao`) | Spring Data JPA (`ProductRepository`/`OrderRepository`) |
| 業務ロジック | `Action` / `Dao` に直書き | `ProductService` / `OrderService` (`@Transactional`) |
| 金額型 | `double` | `BigDecimal` (DB列は `NUMERIC(15,2)`) |
| 接続情報 | ソースにハードコード | `application.yml` + 環境変数 (`demo` / `postgres` プロファイル) |
| スキーマ管理 | `DbInitListener` の Java 埋め込み DDL | Flyway (`db/migration/V1__init.sql`) |
| ログ/例外 | `System.out.println` + 握りつぶし | SLF4J + `@RestControllerAdvice` (409/400/500 の JSON) |
| ビルド/成果物 | Maven war (`mvn tomcat7:run`) | Maven jar (`mvn spring-boot:run`) + Vite |
| テスト | なし | JUnit 5 (Service/Repository/Controller) + Playwright E2E |

業務挙動は変更していません。低在庫判定 (在庫数 < 10)、在庫チェック兼引き落とし
(`UPDATE ... WHERE id = ? AND stock_quantity >= ?`)、小計 = 単価 × 数量、
合計 = 明細小計の総和、注文フロー(数量0以下/未入力・未存在商品はスキップ、在庫不足はエラー、
明細0件はエラー)はいずれも移行前と同一です。

## 業務ドメイン

在庫・注文管理（架空の商社を想定）。

- 商品(在庫)の一覧・検索・低在庫フィルタ・登録・編集
- 注文の一覧・新規作成（複数商品をまとめて注文、在庫引き落とし処理あり）

## 構成

```
pom.xml                                  Spring Boot 3 バックエンド (jar)
src/main/java/com/example/inventory/
  domain/       JPA エンティティ (Product / Order / OrderItem)
  repository/   Spring Data JPA リポジトリ
  service/      業務ロジック (在庫引き落とし・注文作成・検索) と業務例外
  web/          @RestController と @RestControllerAdvice
  dto/          リクエスト/レスポンス DTO (Bean Validation)
  config/       低在庫しきい値・CORS・Clock
src/main/resources/
  application.yml                        demo (H2) / postgres プロファイル
  db/migration/V1__init.sql              Flyway スキーマ + シードデータ
frontend/                                React + Vite + TypeScript
  src/pages/                             商品一覧 / 商品登録・編集 / 注文一覧 / 新規注文
  e2e/                                   Playwright E2E テスト
```

## REST API

| メソッド | パス | 説明 |
|---|---|---|
| GET | `/api/products?keyword=&lowStockOnly=` | 商品一覧(キーワード検索・低在庫フィルタ) |
| GET | `/api/products/{id}` | 商品取得 |
| POST | `/api/products` | 商品登録 |
| PUT | `/api/products/{id}` | 商品更新 |
| GET | `/api/orders` | 注文一覧(明細・合計金額を含む) |
| POST | `/api/orders` | 新規注文作成(在庫引き落としと同一トランザクション) |

エラーは JSON で返却されます: 在庫不足 → `409 INSUFFICIENT_STOCK`、
検証エラー/明細0件 → `400 VALIDATION_ERROR` / `400 EMPTY_ORDER`、その他 → `500 INTERNAL_ERROR`。

## ローカルでの起動方法

### バックエンド (デフォルト: `demo` プロファイル / H2 in-memory)

```bash
mvn spring-boot:run
```

`http://localhost:8080/api/products` で確認できます。起動時に Flyway がスキーマ作成 +
サンプルデータ投入を行います（再起動すると初期状態にリセットされます）。

PostgreSQL を使う場合:

```bash
export DB_HOST=localhost DB_PORT=5432 DB_NAME=inventory DB_USERNAME=... DB_PASSWORD=...
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

### フロントエンド

```bash
cd frontend
npm install
npm run dev     # http://localhost:5173 (/api は :8080 にプロキシ)
```

### 設定 (環境変数)

| 変数 | 既定値 | 説明 |
|---|---|---|
| `DB_URL` / `DB_HOST` / `DB_PORT` / `DB_NAME` | プロファイル依存 | 接続先 |
| `DB_USERNAME` / `DB_PASSWORD` | `sa` / 空 (demo) | 認証情報 |
| `INVENTORY_LOW_STOCK_THRESHOLD` | `10` | 低在庫しきい値 |
| `INVENTORY_CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | CORS 許可オリジン |
| `SERVER_PORT` | `8080` | ポート |

## テスト

```bash
mvn test                       # JUnit 5 (Service / Repository / Controller)
cd frontend && npx playwright install --with-deps chromium
npm run test:e2e               # Playwright E2E (バックエンドを :8080 で起動しておくこと)
```

## 免責事項

本リポジトリはデモ・検証専用のサンプルです。認証・認可、監査ログなど
本番運用に必要な要素は簡略化・省略されています。
