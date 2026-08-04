# Legacy Inventory & Order Management System (Devin マイグレーションデモ用)

このリポジトリは、**Devinによるレガシーシステムのマイグレーション**をお客様にデモするための
サンプルアプリケーションです。「古いJavaシステム → Spring Boot(モダンJava) + React/Vue」への
移行ユースケースを想定しています。

## 現状のシステム構成（移行元 / Before）

| 項目 | 内容 |
|---|---|
| 言語 | Java 7 |
| Webフレームワーク | Struts 1.3.10（2013年にEOL、セキュリティサポート終了） |
| ビュー | JSP + Strutsタグ（bean/html/logic） |
| データアクセス | 生JDBC（ORM未使用） |
| DB | H2埋め込み(デモ用)。本番相当ではOracle/DB2等を想定 |
| ビルド | Maven（`pom.xml`） |
| 認証/サービス層 | なし（Action内に業務ロジックが直接記述） |

### 業務ドメイン

在庫・注文管理（架空の商社を想定）。

- 商品(在庫)の一覧・検索・低在庫フィルタ・登録・編集
- 注文の一覧・新規作成（複数商品をまとめて注文、在庫引き落とし処理あり）

### 画面/機能一覧

| 画面 | URL | 説明 |
|---|---|---|
| 商品一覧 | `/productList.do` | キーワード検索・低在庫のみ表示切替 |
| 商品登録/編集 | `/productEdit.do`, `/productSave.do` | 新規登録・既存商品の編集 |
| 注文一覧 | `/orderList.do` | 注文明細・合計金額を表示 |
| 新規注文 | `/orderEdit.do`, `/orderSave.do` | 商品を選択し数量を入力して注文確定（在庫チェックあり） |

## ローカルでの起動方法

```bash
mvn tomcat7:run
```

`http://localhost:8080/` にアクセス。初回起動時に `DbInitListener` がH2の埋め込みDBへ
テーブル作成＋サンプルデータ投入を行います（再起動すると初期状態にリセットされます）。

> 社内のMavenリポジトリ/プロキシ経由でのビルドを想定しています。
> 依存: `org.apache.struts:struts-core:1.3.10`, `struts-taglib:1.3.10`, `servlet-api:2.5`, `com.h2database:h2`

## このリポジトリにあえて残しているレガシーな課題点（Devinデモの見せ場）

Devinにマイグレーションを依頼した際に「何を・なぜ直したか」を説明しやすくするため、
典型的なレガシーコードのアンチパターンを意図的に残しています。

1. **EOLフレームワーク/言語** : Struts 1（2013年EOL）+ Java 7（2022年サポート終了）
2. **サービス層が存在しない** : 業務ロジック（在庫引き落とし・注文合計計算・検索条件の絞り込み）が
   `Action`クラスや`Dao`クラスに直接書かれている（`OrderSaveAction`, `ProductDao.search()`）
3. **生JDBC + ハードコードされた接続情報** : `DbUtil`にDB接続文字列・認証情報がベタ書き
   （本来はDataSource/application.propertiesへ外部化すべき）
4. **トランザクション境界が曖昧** : `OrderSaveAction`で注文保存と在庫引き落としが別コネクション・
   別トランザクションになっており、片方だけ成功する不整合のリスクがある
5. **例外を握りつぶすエラーハンドリング** : `System.out.println`でログ出力するだけで、
   例外は呼び出し元に伝播しない
6. **JSPに業務ロジック混在** : 低在庫判定の表示分岐やDIYな配列バインディング（`orderForm.jsp`の
   `productIds[] / quantities[]`）がビューに漏れている
7. **UI/UXが古い** : テーブルレイアウト、通貨フォーマットなし（"128000.0 円"のような生の数値表示）、
   レスポンシブ対応なし
8. **自動テストが一つもない**

## 移行先イメージ（After）

- **バックエンド** : Spring Boot（Java 17 以降、Spring Boot対応バージョン）+ Spring Data JPA + REST API
  - `Action`/`Form` → `@RestController` / DTO
  - `Dao`（生JDBC） → Spring Data JPAの`Repository`
  - 在庫引き落とし等の業務ロジック → `@Service` + `@Transactional`
  - DB接続情報 → `application.yml`/環境変数化
- **フロントエンド** : React（または Vue）の別フロントエンドアプリ + REST API連携
  - JSP/Strutsタグ → コンポーネント化されたUI、通貨・日付の適切なフォーマット
  - 旧UIと新UIを並べて見せることで「機能は同じだが体験が大きく改善する」ことを訴求できる
- **テスト** : Service層・Repository層に対する自動テストを新規追加

## Devinへの依頼プロンプト例

```
このリポジトリ（Struts1 + JSP + 生JDBCの在庫・注文管理システム）を、
以下の方針でマイグレーションしてください。

1. バックエンドをSpring Boot（Java 17、REST API）に移行する
   - Struts Action/Form を Controller/DTO に置き換える
   - 生JDBCのDAOをSpring Data JPAのRepositoryに置き換える
   - 在庫引き落とし・注文保存など業務ロジックをServiceクラスに切り出し、
     @Transactionalでトランザクション境界を明確にする
   - DB接続情報はapplication.ymlに外部化する（本番はH2ではなく想定DBに合わせる）
2. フロントエンドをReactの別アプリとして新規実装し、REST API経由でバックエンドと連携する
   - 商品一覧（検索・低在庫フィルタ）、商品登録/編集、注文一覧、新規注文の画面を実装
   - 金額は3桁区切り＋円表記、在庫少は視覚的に強調する
3. 既存の業務要件（在庫チェック、注文明細、合計金額計算）は変更せずに再現する
4. 移行後のバックエンドに対する基本的な単体テストを追加する

まずは移行計画（対象ファイル・作業ステップ・リスク）を提示してください。
```

## 免責事項

本リポジトリはデモ・検証専用のサンプルです。認証・認可、入力値検証、監査ログなど
本番運用に必要な要素は簡略化・省略されています。
