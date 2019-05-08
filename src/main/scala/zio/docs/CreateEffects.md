# Create Effects

ZIO型を作る方法を探る。
一般的なScala型から作る方法、また同期・非同期それぞれの作用を持ったZIO型を作る方法について探る。

## From Success Values

`ZIO.succeed` を使って成功の結果をモデル化できる。

```scala
import scalaz.zio.ZIO
val s1 = ZIO.succeed(42)
```

ZIO型のエイリアスを使って同様のことができる。

```scala
import scalaz.zio.{ZIO, Task}

val s1 = ZIO.succeed(42)
val s2: Task[Int] = Task.succeed(42)
```

succeed メソッドを使うとメソッドの呼び出しの前に値が決定する。  
`ZIO.succeedLazy` を使うと遅延評価できる。

```scala
import scalaz.zio.ZIO

lazy val bigList = (0 to 1000000).toList
lazy val bigString = bigList.map(_.toString).mkString("\n")

val s3 = ZIO.succeedLazy(bigString)
```

`ZIO.succeedLazy` で構築した遅延評価された結果は、その結果が使用される時のみ値を構築する。

## From Failure Values

`ZIO.fail` を使用すると失敗のモデルを作成できる。

```scala
import scalaz.zio.ZIO

val f1 = ZIO.fail("Uh oh!")
```

ZIO型の場合は、エラーの種類に制限はない、  
アプリケーションに適した文字列、例外、またはカスタムデータ型を使用できる。

大抵は Throwable or Exception の拡張クラスを使用して障害をモデル化することを選択する。

`ZIO.succeed` と同様、 `Task` を利用してエイリアスを晴れる。

```scala
import scalaz.zio.Task

val f2 = Task.fail(new Exception("Uh oh!"))
```

## From Scala Values

## Option

`ZIO.fromOption` を使う。

```scala
import scalaz.zio.ZIO

val zoption: ZIO[Any, Unit, Int] = ZIO.fromOption(Some(2))
```

エラーは、 `Unit` になる。 ZIO#mapError を使用して、より具体的なエラータイプに変更できる。

## Either

`ZIO.fromEither` を使う。

```scala
import scalaz.zio.ZIO

val zeither = ZIO.fromEither(Right("Success!"))
```

エラーの場合は `Left`、成功の場合は `Right` になる。

## Try

`ZIO.fromTry` を使う。

```scala
import scalaz.zio.ZIO
import scala.util.Try

val ztry = ZIO.fromTry(Try(42 / 0))
```

エラーの場合は、 `Throwable` になる。

## Function

`A => B` という関数を `ZIO.fromFunction` を使って ZIO型に変換できる。  
これすごい。

```scala
import scalaz.zio.ZIO

val zfun: ZIO[Int, Nothing, Int] = 
  ZIO.fromFunction((i: Int) => i * i)
```

処理を実行するために値の入力値の指定が必要。ここでは入力値の型が Int型。

## Future

`ZIO.fromFuture` を使う。

```scala

import scalaz.zio.{ZIO, Task}
import scala.concurrent.Future

lazy val future = Future.successful("Hello!")

val zfuture: Task[String] = 
  ZIO.fromFuture { implicit ec => 
    future.map(_ => "Goodbye!")
  }
```

fromFuture の関数には ExecutionContext を渡す。  
エラー型は常に Throwable になる。

## From Side-Effects

ZIOは同期・非同期の処理をZIO型に変換することが可能。

## Synchronous Side-Effects

同期処理は、`ZIO.effect` を使用して変換する。  
エラータイプは、常に Throwable になる。

```scala
import scalaz.zio.{ZIO, Task}
import scala.io.StdIn

val getStrLn: Task[Unit] =
  ZIO.effect(StdIn.readLine())
```

例外がスローされないことがわかりきっているなら、 `ZIO.effectTotal` が使える。

```scala
import scalaz.zio.{ZIO, UIO}

def putStrLn(line: String): UIO[Unit] =
  ZIO.effectTotal(println(line))
```

Throwable のサブタイプを投げることがわかっているなら `ZIO.refineOrDie` を使用できる。

```scala
import scalaz.zio.{ZIO,IO}
import java.io.IOException
import scala.io.StdIn

val getStrLn2: IO[IOException, String] =
  ZIO.effect(StdIn.readLine()).refineOrDie {
    case e : IOException => e
  }
```

## Asynchronous Side-Effects

コールバックベースのAPIを使用した非同期の副作用は、`ZIO.effectAsync` を使用してZIO型へ変換できる。

```scala
import scalaz.zio.IO
trait User
trait AuthError

object legacy {
  def login(
    onSuccess: User      => Unit, onFailure: AuthError => Unit): Unit = ???
}

val login: IO[AuthError, User] = 
  IO.effectAsync[AuthError, User] { callback =>
    legacy.login(
      user => callback(IO.succeed(user)),
      err  => callback(IO.fail(err))
    )
  }
```

割り込み、リソースの安全性、エラーハンドリングの点で、ZIO型の恩恵が受けられる。

## Blocking Synchronous Side-Effects

scalaz.zio.blocking パッケージにプロッキングIOを安全にZIO型に変換するモジュールが含まれる。  
effectBlocking メソッドを使ってZIO型に変換することができる。

 ```scala
 import scalaz.zio.blocking._
 
 val sleeping = 
   effectBlocking(Thread.sleep(Long.MaxValue))
 ```

ブロッキング処理のための別のスレッドプールで実行される。  
すでにZIO型に変換済みの場合は、 `blocking` メソッドを使用してブロッキングスレッドプールにシフトできる。

```scala
import scalaz.zio._
import scalaz.zio.blocking._
import scala.io.{ Codec, Source }

def download(url: String) =
  Task.effect {
    Source.fromURL(url)(Codec.UTF8).mkString
  }

def safeDownload(url: String) = 
  blocking(download(url))
```
