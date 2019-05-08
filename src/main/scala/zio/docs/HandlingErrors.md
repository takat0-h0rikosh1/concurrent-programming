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

Scala の Option と Either のデータ型が fold を使えるように、  
ZIO でも fold が使える。

`fold` は関数を適用して使う、下記のように失敗の結果を無効化する。

```scala
import scalaz.zio.{IO, UIO}
import java.io.IOException

lazy val DefaultData: Array[Byte] = ???
def openFile(path: String): IO[IOException, Array[Byte]] = ???

val z: UIO[Array[Byte]] =
  openFile("primary.data").fold(
      _ => DefaultData,
      data => data)
```

`foldM` も同様に関数を適用するが、失敗と成功のどちらも処理することができる。

```scala
import scalaz.zio.{IO, ZIO}
import java.io.IOException

lazy val DefaultData: Array[Byte] = ???
def openFile(path: String): IO[IOException, Array[Byte]] = ???

val z: IO[IOException, Array[Byte]] = 
  openFile("primary.data").foldM(
    _    => openFile("secondary.data"),
    data => ZIO.succeed(data))
```

## Retrying

