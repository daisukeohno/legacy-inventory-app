# Inventory & Order Management System (Spring Boot + React)

在庫・注文管理システム。旧実装（Struts 1.3.10 + JSP + 生JDBC / Java 7）を
**Spring Boot 3 (Java 17) の REST API バックエンド + React (Vite/TypeScript) フロントエンド**へ移行したもの。
業務要件（低在庫判定・商品検索・在庫引き落とし・注文確定・合計金額計算）は旧実装と同一。

## 構成

| ディレクトリ | 内容 |
|---|---|
| `backend/` | Spring Boot 3.3 / Java 17 / Spring Data JPA / H2 の REST API |
| `frontend/` | Vite + React + TypeScript のシングルページアプリ |

### 旧実装からの対応関係

| Before (Struts 1) | After |
|---|---|
| `action/*Action` + `form/*Form` | `web/*Controller` + `dto/*` (Bean Validation) |
| `dao/*Dao`（生JDBC） | `repository/*Repository`（Spring Data JPA） |
| `Action` 内の業務ロジック | `service/ProductService`, `service/OrderService`（`@Transactional`） |
| `DbUtil` のハードコード接続情報 | `application.yml`（環境変数で上書き可能） |
| `DbInitListener` の DDL/seed | `src/main/resources/schema.sql` / `data.sql` |
| JSP + Struts タグ | React コンポーネント（`frontend/src/pages`） |
| `System.out.println` での握り潰し | `@RestControllerAdvice`（400 / 404 / 409 + エラーメッセージ JSON） |

在庫引き落とし（`ProductDao.decreaseStock`）と注文保存（`OrderDao.save`）が別トランザクションで
片方だけ成功し得た旧実装のバグは、`OrderService.create` の単一トランザクションに統合して解消している。

## 起動方法

### バックエンド

```bash
cd backend
./mvnw spring-boot:run
```

`http://localhost:8080` で起動し、起動時に H2 インメモリDBへスキーマ作成＋初期データ投入を行う
（`schema.sql` / `data.sql`。再起動で初期状態にリセット）。

主なエンドポイント:

| メソッド | パス | 説明 |
|---|---|---|
| GET | `/api/products?keyword=&lowStockOnly=` | 商品一覧・検索（名前/SKUの部分一致、低在庫フィルタ） |
| GET | `/api/products/{id}` | 商品取得 |
| POST | `/api/products` | 商品登録 |
| PUT | `/api/products/{id}` | 商品更新 |
| GET | `/api/orders` | 注文一覧（明細・合計金額つき） |
| POST | `/api/orders` | 注文確定（在庫チェック＋在庫引き落とし） |

DB接続情報・CORS 許可オリジンは環境変数で上書きできる
（`DB_URL` / `DB_USERNAME` / `DB_PASSWORD` / `DB_DRIVER` / `CORS_ALLOWED_ORIGINS`）。

`schema.sql` は `DROP TABLE` から始まるため、`spring.sql.init.mode` は既定で `embedded`（組み込みDBのときのみ実行）。
永続DBに対して意図的に初期化したい場合のみ `SQL_INIT_MODE=always` を指定する。
H2 コンソールは既定で無効で、`H2_CONSOLE_ENABLED=true` でのみ有効化される。

### フロントエンド

```bash
cd frontend
npm install
npm run dev
```

`http://localhost:5173` で起動。`/api` へのリクエストは Vite の dev プロキシ経由で
`http://localhost:8080`（`VITE_BACKEND_URL` で変更可）へ転送される。
Node.js は 20.19+ または 22 系が必要（`.nvmrc` は 22）。

## テスト

```bash
cd backend
./mvnw test
```

- `OrderServiceTest`: 在庫チェック・在庫引き落とし・合計金額計算・在庫不足時のロールバック（注文も在庫も更新されない）
- `ProductServiceTest`: キーワード部分一致・低在庫フィルタ
- `ProductRepositoryTest`(`@DataJpaTest`): 在庫引き落としクエリ（在庫十分/不足）

## 免責事項

本リポジトリはデモ・検証専用のサンプルです。認証・認可、監査ログなど本番運用に必要な要素は簡略化・省略されています。
