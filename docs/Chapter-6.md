# タスクの実行

並行アプリケーションの多くは、複数のタスクの実行を軸に編成される。  
タスクは仕事の抽象的で独立した単位。

## タスクをスレッドで実行する

タスクはそれぞれ独立した活動。独立した活動とはほかのタスクのステートや結果や副作用に依存しない仕事。  

サーバーアプリケーションは正常な負荷状態で良いスループットと良い応答性の両方が必要。  
さらに過負荷になったときに急なエラーになるのではなく穏やかな性能低下が求められる。  
良質な実行ポリシーを実施すればこれらの目標を達成しやすくなる。

### タスク境界

それは個々のクライアントリクエスト。

Webサーバ、メールサーバ、ファイルサーバ、EJBコンテナ、データベースサーバ。  
上記はどれもリモートを介してリモートクライアントからのリクエストを受け付ける。

## タスクを逐次的に実行する

```java
// 逐次処理のWebサーバ
class SingleThreadWebServer {
  public static void main(String[] args) throws IOException {
    ServerSocket socket = new ServerSocket(80)
    while (true) {
      Socket conection = socket.accept();
      handleRequest(connection);
    }
  }
}
```

シンプルで理論的には正しいプログラムだが、一度に一つのリクエストしか扱えないので、  
性能が悪すぎて実際の業務には使えない。

リクエストには、計算処理とI/Oが入り混じっている。サーバーはソケットI/Oを実行してリクエストを  
読みレスポンスを書き出す。この活動はネットワークが渋滞したり接続性に問題があるとブロックする。

ファイルI/OやデータベースI/Oがある場合も同様のことが言える。

シングルスレッドのサーバは現在のリクエストを完了を遅らせるだけでなく、待機しているリクエストの処理開始にも影響する。

## タスクのためのスレッドを明示的に作る

応答性の良いやり方は、各リクエストにサービスする新しいスレッドを作ること。

```java
class ThreadPerTaskWebServer {
  public static void main(String[] args) throws IOException {
    ServerSocket socket = new ServerSocket(80);
    while (true) {
      final Socket connection = socket.accept();
      Runnable task = new Runnable() {
        public void run() {
          handleRequest(connection);
        }
      }
      new Thread(task).start;
    }
  }
}
```

リクエストを処理するためにスレッドを立てている。  
メインスレッドのなかではリクエストの処理をせず、すぐに次のリクエストを受け付けることができる。  

## 制限のないスレッド作成の欠点

タスク毎にスレッドを与えるのは欠点がいくつかある。

### スレッドのライフサイクルとオーバーヘッド

スレッドの作成にはオーバーヘッドが発生するのでリクエストの処理に遅延が発生する。  
また、JVMやOSも仕事をするので大量のリクエストをさばくのに相当量の計算資源を要する。

### 資源の消費

アクティブなスレッドはメモリを食うので実行状態のスレッドが借りようなプロセッサより多いと、  
スレッドは停滞し、むしろサーバの能力を劣化させる。

### 安定性

作れるスレッドの数には上限がある。無制限にスレッドを作るとOOMEが発生しアプリケーションがクラッシュする。並行処理の事故は、プロトタイプなどで一見まともに動くプログラムが、大きな負荷で実働したときに死ぬタイプが多い。

## Executorフレームワーク

- 前提
   - 逐次的な実行は応答性とスループットが悪い
   - タスク毎にスレッドを与えるのは資源管理がお粗末

アプリケーションが過負荷でメモリ不足にならないためにサイズ制限のあるキューを使う方法がある。  
※第五章参照

スレッドプールは同じ考え方でスレッドを管理する。  
java.util.concurrent パッケージに柔軟な使い方のできるスレッドプールの実装がある。  
これは Executor フレームワークの構成要素になっている。

```java
public interface Executor {
  void execute(Runnable command);
}
```

- Executor はタスクを Runnable で書き表す。
- タスクを実行すること/実行のされ方と、タスクの実行をJVMに依頼することの両者を分離する
- ライフサイクルのサポート、統計収集のためのフック、アプリケーションの管理や関しなどの機能も提供する。

Executor はプロデューサー・コンシューマパターンを使って構築されている。

- タスクを依頼する活動がプロデューサ（実行すべき単位を消費する）
- タスクを実行するスレッドがコンシューマ

アプリケーションをプロデューサー・コンシューマ方式で設計する一番楽な方法が、 Executor を使うこと。

### Executor を使った Web サーバ

裸のスレッド作成をやめて Executor を使う。

```java
class TaskExecutionWebServer {
  private static final int NTHREADS = 100;
  private static final Executor exec = Executors.newFixedThreadPool(NTHREADS);

  public static void main(String[] args) throws IOException {
    ServerSocket socket = new ServerSocket(80);
    while (true) {
      final Socket connection = socket.accept();
      Runnable task = new Runnable() {
        public void run() {
          handleRequest(connection);
        }
      };
      exec.execute(task);
    }
  }
}
```

各リクエストに対し新しいスレッドを作ったり、逐次的に実行するようにするには、  
Executor を implements してニーズ合った独自の Executor を実装すると良い。

### 実行ポリシー

タスクを分離できることのうまみは実行ポリシーの指定のしやすさ。  
実行ポリシーの例は下記。

- タスクをどのスレッドで実行するのか？
- タスクをどんな順序で実行するのか（FIFOか？LIFOか？プライオリティ順か？）
- 並行に実行できるタスクはいくつか？
- 待ち行列（キュー）に並ぶタスク数の最大数はいくつか？
- 過負荷のためにタスクを拒絶するときは、どんな基準で犠牲者を選ぶのか？アプリケーションへの通知はどうやるのか？
- タスクを実行する前と実行した後でどんなアクションを行うべきか？

並行タスク数を制限するとアプリケーションの資源枯渇や希少資源の奪い合いによる性能劣化が回避できる。

### スレッドプール

- ワーカースレッドのプールを管理する
- タスクを入れておくワークキューのサイズ制約がある

#### ワーカースレッドの人生

1. ワークキューに次のタスクをリクエスト
2. 実行
3. 他のタスクを待つ

#### スレッドプールを使ってタスクを実行すると...

- 複数リクエストの処理費用低減
- スレッド作成待ち時間がタスクの実行を遅らせることがない

つまり応答性が良くなる。

#### 既成のスレッドプールを使う

Executors クラスの static ファクトリメソッドのどれかを使う。

##### .newFixedThreadPool

- タスクの依頼されると固定サイズのスレッドプールを作成する。  
- プールサイズを一定に保つため、スレッドが死んだら新たなスレッドを加える。

##### .newCachedThreadPool

- プールサイズを柔軟に変更する。
- プールサイズが処理要求の数を超えていたらスレッド数を減らす
- 要求が増えたらあらたばスレッドを加える

##### .newSingleThreadExecutor

- 一つのワーカースレッドを作ってタスクを処理する
- タスクキューが課す順序規則(FIFO, LIFO, プライオリティ順)にしたがって逐次的に処理する。
- ワーカースレッドが死んだら新しく作る

##### .newScheduledThreadPool

- タスクの遅延開始と周期的な実行をサポートする固定サイズのスレッドプール
- java.util.Timer に似ている

ThreadPoolExecutor クラスを使って独自の Executor を構築することも可能。

### Executor のライフサイクル





##### .newSingleThreadPool

##### .newScheduledThreadPool



