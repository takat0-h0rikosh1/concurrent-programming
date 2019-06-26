package zio

import scalaz.zio.{UIO, ZIO}
import scalaz.zio.Task
import scala.concurrent.Future
import scala.util.Try

object CreatingEffects extends App {

  // 単に成功の結果を返すのみ、遅延評価されない
  // 要件を受け取ることはなく、失敗もしない
  val s1: UIO[Int] = ZIO.succeed(42)
  val s2: Task[Int] = Task.succeed(42)

  // 遅延評価する
  val s3_1 = ZIO.succeedLazy(42)

  //  succeed メソッドを使うとメソッドの呼び出しの前に値が決定する。
  //  `ZIO.succeedLazy` を使うと遅延評価できる。
  lazy val bigList = (0 to 1000000).toList
  lazy val bigString = bigList.map(_.toString).mkString("\n")
  val s3_2: UIO[String] = ZIO.succeedLazy(bigString)
  s3_2.map(s => ZIO.succeed(s))

  // 失敗のモデルを作成
  val f1 = ZIO.fail("Uh oh!")
  val f2 = Task.fail(new Exception("Uh oh!"))

  //  From Scala Values
  // from Option
  val zoption: ZIO[Any, Unit, Int] = ZIO.fromOption(Some(2))

  // from Either
  val zeither = ZIO.fromEither(Right("Success!"))

  //  from Try
  val ztry = ZIO.fromTry(Try(42 / 0))

  // from function
  val zfun: ZIO[Int, Nothing, Int] =
    ZIO.fromFunction((i: Int) => i * i)

  //  from future
  lazy val future = Future.successful("Hello!")
  val zfuture: Task[String] =
    ZIO.fromFuture { implicit ec =>
      future.map(_ => "Goodbye!")
    }

  import scala.io.StdIn

  val getStrLn: Task[Unit] =
    ZIO.effect(StdIn.readLine())
}
