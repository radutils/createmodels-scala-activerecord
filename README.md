## Scala ActiveRecord 向けモデルファイル生成ツール

user,password,host,DBを指定すると該当DBに登録されている全テーブルのModelクラスを生成する。なお、対象DBは、MySQLとする。



### 機能

- 主キーが`id`でない場合、コンストラクタ定義でアノテーション`@Column`で指定する。
  - 例 主キーがseqの場合、以下のようになる
    - `case class hoge(@Column("seq") override val id: Long) extends ActiveRecord {}`
- テーブル内のカラム数がscalaの22制限を超える場合、クラス変数で主キー以外を定義する。本プログラムでは主キー、外部キー、タイムスタンプ(`created_at`,`updated_at`)を除く15個を上限とする。
- リレーションがあるカラムに関しては `lazy var` または `lazy val`で定義する。ただしvar/valは選択できるようになっていない。
  - カラム内に `client_id`がある場合 `lazy val client = belongsTo[Client]`
- Nullを許容しているカラムは `Option`型で定義する。
- NotNullのカラムに、コンストラクタ定義、クラス変数定義でアノテーション`@Required`を付与する。
- VarCharのカラムに、コンストラクタ定義、クラス変数定義でアノテーション`@Length(min=0 max=カラムサイズ)`を付与する。
- UnSigenedのカラムに、コンストラクタ定義、クラス変数定義でアノテーション`@Range(min=0 max=Long.MaxValue)`を付与する。
- カラムがScalaの予約語または、頭文字が大文字の場合に、コンストラクタ定義、クラス変数定義でアノテーション`@Column`を付与する。また変数名は先頭にアンダースコア(_)を付与し、先頭の大文字を小文字に変換する。
  - 予約語packageがカラム名の場合 `@Column("package") val _package: String`
  - カラム名が大文字の場合 `@Column("Testcolumn") val _testcolumn: String`
- カラムに`created_at`, `updated_at`が存在する場合は　`with Datestamps`をつけて `case class`を宣言する。
- カラムにコメントが付いている場合以下のMapを定義する。
  - val  labelName:Map[String,String] = (("#{columnName}" -> "#{comment}"), ....)



### 使い方

**取得**

    $ git clone https://github.com/radutils/createmodels-scala-activerecord.git
    $ cd ./createmodels-scala-activerecord

**コンパイル**

    $ ./sbt compile

**実行**

    $ ./sbt run -H [サーバーアドレス] -P [ポート番号] -D [データベース名] -u [DB接続ユーザ名] -p [接続ユーザパスワード] -o [Modelクラス生成ファイル保存先]

**実行プログラムの作成と作成プログラムの起動**

    $ ./sbt pack
    $ target/pack/bin/Parse -H [サーバーアドレス] -P [ポート番号] -D [データベース名] -u [DB接続ユーザ名] -p [接続ユーザパスワード] -o [Modelクラス生成ファイル保存先]

**実行プログラムのインストール**

    $ ./sbt pack
    $ cd target/pack; make install
    $ ~/local/bin/Parse -H [サーバーアドレス] -P [ポート番号] -D [データベース名] -u [DB接続ユーザ名] -p [接続ユーザパスワード] -o [Modelクラス生成ファイル保存先]



### 実行プログラムのコマンドライン引数について

**-H [サーバアドレス]**

データベースサーバの接続アドレスを指定する。デフォルト値は、127.0.0.1。

**-P [ポート番号]**

データベースサーバの接続ポート番号を指定する。デフォルト値は、3306。

**-D [データベース名]**

実行時必須引数。解析するデータベースサーバのデータベース名を指定する。デフォルト値はなし。

**-u [DB接続ユーザ名]**
       
実行時必須引数。データベースサーバに接続可能なユーザ名を指定する。デフォルト値はなし。

**-p [接続ユーザパスワード]**

データベースサーバに接続するユーザのパスワードを指定する。デフォルト値はなし。
       
**-o [Modelファイル出力先ディレクトリ ]**:

実行時必須引数。解析したデータの出力先ディレクトリを指定する。デフォルト値はなし。
       
**--help**

コマンドライン引数の説明を表示する。
