# Basic Operations

## Mapping

ZIO#map メソッドを利用することで正常系にマッピングできる。  
ZIO#mapError によりエラーのケースに対しマッピングできる。

```scala
import scalaz.zio._

val z: UIO[Int] = IO.succeed(21).map(_ * 2)
val zf: IO[Exception, Unit] = 
  IO.fail("No no!").mapError(msg => new Exception(msg))
```

それぞれのマッピングは根本的な失敗と成功の結果を変更するようなことはしない。

## Chaining

ZIO#flatMap を使うことで2つの処理を順序を保って実行できる。

```scala
import scalaz.zio.{IO, UIO}

val z: UIO[List[Int]] = 
  IO.succeed(List(1, 2, 3)).flatMap { list =>
    IO.succeed(list.map(_ + 1))
  }
```

## For Comprehensions

flatMap, map を使えるので当然 for 式も使うことができる。

```scala
import scalaz.zio.console._

val program = 
  for {
    _ <- putStrLn("Hello! What is your name?")
    n <- getStrLn
    _ <- putStrLn(s"Hello, ${n}, welcome to ZIO!")
  } yield ()
```
