# Handling Errors

## Either

ZIO#Either でエラーの結果を表現することができる。  
`ZIO[R, E, A]` から `ZIO[R, Nothing, Either[E, A]]` を生成する。

```scala
import scalaz.zio.{IO, UIO}

val zeither: UIO[Either[String, Int]] = 
  IO.fail("Uh oh!").either
```

ZIO#absolve を使用することで逆転できる。

```scala
import scalaz.zio.{ZIO, IO, UIO}

def sqrt(io: UIO[Double]): IO[String, Double] =
  ZIO.absolve(
    io.map(value =>
      if (value < 0.0) Left("Value must be >= 0.0")
      else Right(Math.sqrt(value))
    )
  )
```

## Catching All Errors

`catchAll` を使うと全パターンのエラーに対する回復処理を定義できる。

```scala
import scalaz.zio.IO
import java.io.IOException

def openFile(path: String): IO[IOException, Array[Byte]] = ???

val z: IO[IOException, Array[Byte]] = 
  openFile("primary.json").catchAll(_ => 
    openFile("backup.json"))

```

## Catching Some Errors

パターンマッチしたい場合は `catchSome` を使う。

```scala
import scalaz.zio.IO
import java.io.IOException
import java.io.FileNotFoundException

def openFile(path: String): IO[IOException, Array[Byte]] = ???

val data: IO[IOException, Array[Byte]] = 
  openFile("primary.data").catchSome {
    case _ : FileNotFoundException => 
      openFile("backup.data")
  }
```

## Fallback

`orElse` コンビネーターを使ってエラー時に実行する別の処理を定義できる。

```scala
import scalaz.zio.IO
import java.io.IOException

def openFile(path: String): IO[IOException, Array[Byte]] = ???

val z: IO[IOException, Array[Byte]] = 
  openFile("primary.data") orElse openFile("backup.data")
```

## Folding

// TODO Folding について
