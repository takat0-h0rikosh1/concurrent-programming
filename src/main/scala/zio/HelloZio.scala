package zio

import java.io.IOException

import scalaz.zio.{App, ZIO}
import scalaz.zio.console._

object HelloZio extends App{

  def run(args: List[String]): ZIO[Console, Nothing, Int] =
    myAppLogic.fold(_ =>1, _ => 0)

  val myAppLogic: ZIO[Console, IOException, Unit] =
    for {
      _ <- putStrLn("Hello! What's your name")
      n <- getStrLn // CLI入力値を受け取る
      _ <- putStrLn(s"Hello, $n, welcome to ZIO!")
    } yield {}
}
