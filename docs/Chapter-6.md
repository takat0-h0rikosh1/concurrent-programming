# タスクの実行

並行アプリケーションの多くは、複数のタスクの実行を軸に編成される。  
タスクは仕事の抽象的で独立した単位。

## タスクをスレッドで実行する

タスクはそれぞれ独立した活動。独立した活動とはほかのタスクのステートや結果や副作用に依存しない仕事。  

独立したタスクは並行処理に向いている。

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

Executor をシャットダウンしないとJVMを終了できない。

Executor は複数のタスクを非同期で処理するので、おだやかなシャットダウンから唐突なシャットダウンまで、
様々な形式のシャットダウンができななければならない。

実行サービスのライフサイクルという問題に対応するために、
Executor を extends したサブインターフェイス ExecutorService にライフサイクルを
管理するメソッドが用意された。

```
public interface ExecutionService extends Executor {
  void shutdown();
  List<Runnable> shutdownNow();
  boolean isShutdown();
  boolean isTerminated();
  boolean awaitTermination(long timeout, TimeUnit unit)
    throws InterruptedException
}
```

ExecutorService が暗黙に定義しているライフサイクルは３つステートがある。

- 実行中
- シャットダウン中
- 終了

#### 各メソッドの説明

- shutdown()
  - おだやかなシャットダウンを行う
　- 既に依頼済のタスクも完了する
　- 新たなタスクは受付けない
- shutdownNow()
  - 唐突なシャットダウンを行う
  - 実行中のタスクはキャンセルする
  - キューにあってまだ始まってないタスクはスタートしない
- awaitTermination(...)
  - ExecutionServiceが終了ステートへ到達するのを待つ
- isTerminated()
  - 終了しているかしてないかを調べる 

### タスクの遅延開始と周期的実行

java.util.Timer クラスはタスクの遅延実行(このタスクを100ミリ秒後に実行せよ)
と周期的実行(このタスクを10ミリ秒間隔で実行せよ)を管理する。

しかし、Timerには欠点がいくつかあるので、ScheduledThreadPoolExecutorを使うべき。

#### TimerとScheduledThreadPoolExecutorのスケジューリング比較

- Timer	
   - スケジューリングは、相対時間ではなく絶対時間に基づいて行われている。
   - なのでタスクがシステムクロックの変化に影響を受けることがある。
- ScheduledThreadPoolExecutor
   - 相対時間だけをサポートする。

※Javaのversion upで解決しているかもしれない。

#### TimerのTimerTask実行の挙動

作成されるスレッドはひとつだけ。
したがってタスクの実行が長くかかると、他のタスクの実行開始に影響する。
10ミリ間隔のタスクと、40ミリ秒かかるタスクを同時に実行することはできない。

#### ScheduledThreadPoolExecutorのタスク実行の挙動

遅延開始タスクや周期的なタスクのために複数のスレッドを使って回避する。

#### Timerの他の問題

TimerTaskがチェックされない例外(RuntimeException)を投げたときの対応がお粗末。Timerはその例外をcatchしないのでこの例外はスレッドを終了してしまう。
TimerはThreadを復活させることもしないので未実行のTimerTaskは実行されないし、
新しいタスクはスケジューリングされない。

https://docs.oracle.com/javase/jp/8/docs/api/java/util/Timer.html

Java 5.0以降はTimerを使う理由はほとんどない。


```java
public class OutOfTime {
  public static void main (String[] args) throws Exception {
    Timer timer = new Timer();
    timer.schedule(new ThrowTask(), 1);
    SECONDS.sleep(1);
    timer.schedule(new ThrowTask(), 1);
    SECONDS.sleep(5);
  }

  static class ThrowTask extends TimerTask {
    public void run() { throw new RuntimeException(); }
  }
}
```

#### スケジューリングサービスを自作する

BlockingQueueの実行クラス、DelayQueueを利用する。
このキューにはScheduledThreadPoolExecutorのようなスケジューリング機能がある。

### 並列化できる箇所/すべき箇所を見つける

Executorを使うためにはタスクをRunnableとして書き表す必要がある。
サーバアプリケーションが受けるひとつひとつのクライアントリクエストには自明なタスク境界がある。
ひとつのクライアントリクエストの中にもさらに並列化できそうな箇所がある。
データベースサーバーなどはその典型。

#### 逐次的なページレンダラ

割愛。

#### 結果を返すタスク: CallableとFuture

Executor はタスクをRunnableとして表現する。

##### Runnableには制約がある。

- runメソッドは値を返さず、チェックされる例外も返さない。
- 副作用のある処理は可能。

##### Callableを使う

タスクの典型は下記

- データベースのクエリ結果
- ネットワークからリソースを取ってくる
- 複雑な関数の計算

このようなタスクの表現としてはCallableが適している。
callメソッドは値を返し、例外も投げることができる。

ExecutorsにはRunnableのタスクをCallableでラップするメソッドがある。

##### タスクのライフサイクル

作成(created)、依頼(submitted)、開始(started)、完了(completed)で表現される。

#### Futureを使う

Futureはひとつのタスクのライフサイクルのあらゆる段階を表現するオブジェクト。
タスクの完了やキャンセルを調べるメソッド、結果を取り出すメソッド、キャンセルするメソッドなどが揃っている。

##### Future.getの振る舞い

- タスクがすでに完了していたらリターンするか例外を投げる
- 完了していなければ完了するまでブロックする
- タスクが例外を投げて完了したら、ExecutionExceptionでラップして再投する
- タスクがキャンセルされてたら、CancellationExceptionを投げる

##### Futureを使ってタスクを表現するには...

まずExecutorService#submitにRunnableやCallableを依頼してFutureをもらう。
それを使って結果を取り出したりタスクをキャンセルしたりする。

FutureTaskを明示的に作り、Executorに依頼して実行したり、runメソッドを読んで直接実行したりする。

RunnableやCallableをExecutorに依頼すると、そのRunnableやCallableが依頼したスレッドからタスクを実行するスレッドへ安全に公開される。同様にFutureの結果の値をセットすると、結果を計算したスレッドからそれをgetで取り出すスレッドへ、結果が安全に公開される。

##### Futureを使うページレンダラ

- テキストのレンダーと画像のダウンロードとでタスクを分ける。
- 全画像のダウンロードをCallableのタスクとし、Executor#submitに渡してFutureを受け取る。そのすきにテキストをレンダーする。
- Future.getしたときのInterrunptExceptionとExecutionExceptionに備える。
- 本来は画像1件ずつのダウンロードを並列実行できるのが良い。

// TODO コード写経

#### 異質なタスクを並列化する限界

複数のワーカーにそれぞれ異質なタスクを割り当てると、タスクのサイズが大小バラバラになる可能性がある。仕事を２つに分割したタスクAとBを二人のワーカーに割当、AがBの10倍時間がかかるなら、両者の並列化によるスピードアップの効果はわずかに9%。タスクを複数のワーカーに分割して割り当てると必ず調整作業のオーバーヘッドが生じる。分割が有意義であるためには、並列化による生産性の向上がこのオーバーヘッドを大きく上回っている必要がある。

上の「Futureを使うページレンダラ」はテキストのレンダリングのほうが画像のダウンロードよりも圧倒的にはやい。なので、性能は逐次処理とそんなに変わらないのにコードが複雑になった。２つのスレッドを使ったときの最大の期待値はスピードが２倍になること。しがって互いに異質な活動を並列化しようとすると辛い場合がある。

#### CompeletionService: ExecutorがBlockingQueueと合体

CompleteServiceインタフェイスはExecutorとBlockingQueueの機能を合わせて持っている。これにCallableのタスクを依頼すると、takeやpollなどのキューのようなメソッドを使って完了した結果を取り出せる。これらのメソッドはFutureを返すので結果がすでにできていればFuture.getで取得できる。ExecutorCompletionServiceはCompletionServiceの実装クラスで、タスクの実行をExecutorに委譲する。

ExecutorCompletionServiceの実装はとても簡単。コンストラクタにQueueingFutureでラップされる。これはFutureTaskのサブクラスで、オーバーライドしたdoneメソッドが結果をBlockingQueueに入れる。takeメソッドとpollメソッドはBlockingQueueに委譲される。結果がまだなければブロックする。

```java
private class QueueingFuture<V> extends Future<V> {
  QueueingFuture(Callable<V> c) { super(c); }
  QueueingFuture(Runnable t, V r) { super(t, r); }

  protected void done() {
    completionQueue.add(this);
  }
}
```

#### ex.) 例: CompletionServiceを使ったページレンダラ

CompletionServiceを使うとページレンダラの実行性能を２つの点で向上できる。
実行時間の短縮と応答性を改善できる。画像を一括でダウンロードしていたのを、
一つひとつダウンロードするために別々のタスクを作り、それらをスレッドプールで実行して、
逐次的なダウンロードを並列的な処理に変える。
これにより、すべての画像をダウンロードするための時間が短縮する。そして結果をCompletionServiceから取り込み、
それぞれの画像が可利用になるとすぐに表示するので動的で応答性の良いユーザインタフェイスを提供できる。

// TODO コード写経

#### タスクに制限時間を設ける

ときには、活動が一定の時間内に完了しなかったらその結果は要らないし、活動を放棄したいこともある。

- ex.1) 外部の広告サーバから広告を取り込むWebサーバ
  - 広告を2秒以内に取り込めないとデフォルトの広告を表示するので、広告の取り込みを中断しても支障はない。
- ex.2) 複数のデータソースからデータを並列的に取り込むポータルサイトは
  - 一定時間だけデータを待ち、それが過ぎたらそのデータのないページを表示する。

Future.get

- 結果が得られたらただちにリターン
- タイムアウトまでに結果が得られなければTimeoutExceptionを投げる

待ち時間が長すぎたらタスクを停止して計算資源の浪費を防ぐ。

#### 例: 旅行予約ポータル

前の説で説明した時間制限のやり方は、複数のタスクにも簡単に応用できる。
予約ポータルの例で考える。

ユーザが旅行の日程と要求を入力するとポータルは複数の航空会社、ホテル、レンタカー会社などから
ビッドを取り込んで表示します。会社によってはビットを用意するためのWEBサービスを呼び出し、
データベースを検索し、EDIのトランザクションを実行し、そのほかいろんな計算処理をするだろう。

ポータル側としては、ページの応答時間がビット提示の一番遅い会社のリスポンスにするのではなく、
一定の時間内に得られた情報だけをユーザに提示したいでしょう。時間内に応答のない会社に関しては
彼らの情報を省略するかまたは、"Java航空未応答" といったプレースホルダーを表示するだろう。

一つの企業からビットを取り込む処理は、ほかの企業からビッドを取り込むことから独立しているので、
単一のビッド取り込みはビッドの取得を並列処理で行うための妥当なタスク境界だ。

タスクをn個作ってスレッドプールに依頼し、それらのタスクを複数のFutureで管理し、時間指定付きのFuture.getで
結果を逐次的に取り込むのは簡単だ。しかし、もっと簡単な方法がある。それは ExecutorServiceのinvokeAllだ。

時間付きのinvokeAllを使って複数のタスクのコレクションを引数に取り、Futureのコレクションを返す。
この２つのコレクションは構造が同じ。invokeAllはタスクのコレクションのイテレータが使う順序でFutureを返し、
値のコレクションに加える。

そこで呼び出し側は、Futureとそれが表しているCallableを結び付けられる(FutureにそのFutureのCallableにアクセスするメソッドは残念ながらない)。
時間指定付きのinvokeAllは、全てのタスクが完了したとき、または呼び出しているスレッドがインタラプトされたとき、またはメソッドが時間切れになったときにリターンする。
invokeAllがリターンしたとき、各タスクは正常に完了しているかまたはキャンセルされているかのどちらか。

クライアントのコードはgetやisCancelledを読んで、どっちの結果だったかを知ることができる。

## まとめ

Executorフレームワークを使うとタスクの依頼と実行ポリシーを分離でき豊富な種類の実行のポリシーを使える。
今後はタスクを実行するためにスレッドを使いたくなったら代わりにExecutorの利用を検討すること。
