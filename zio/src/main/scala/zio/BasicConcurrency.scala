package zio

import scalaz.zio.{Fiber, IO, UIO, ZIO}

object BasicConcurrency {

  def main(arg: Array[String]): Unit = awaitingFibers

  def fib(n: Long): UIO[Long] =
    if (n <= 1) {
      UIO.succeed(n)
    } else {
      println(s"${n - 1} + ${n -2} = ${(n -1)  + (n-2)}")
      fib(n - 1).zipWith(fib(n - 2))(_ + _)
    }

//  val z: UIO[Fiber[Nothing, Long]] =
//    for {
//      fiber <- fib(100).fork
//    } yield fiber

  def joiningEffects: ZIO[Any, Nothing, String] =
    for {
      fiber <- IO.succeed("Hi!").fork
      message <- fiber.join
    } yield message

  def awaitingFibers =
    for {
      fiber <- fib(10).fork
      exit <- fiber.await
    } yield exit

  def interruptFibers =
    for {
      fiber <- IO.succeed("Hi!").forever.fork
      exit <- fiber.interrupt
    } yield exit

  def interruptFibers2 =
    for {
      fiber <- IO.succeed("Hi!").forever.fork
      exit <- fiber.interrupt.fork
    } yield ()

  def composingFibers =
    for {
      fiber1 <- IO.succeed("Hi!").fork
      fiber2 <- IO.succeed("Bye!").fork
      fiber   = fiber1 zip fiber2
      tuple  <- fiber.join
    } yield tuple

  def composingFibers2 =
    for {
      fiber1 <- IO.fail("Uh oh!").fork
      fiber2 <- IO.succeed("Hurray!").fork
      fiber   = fiber1 orElse fiber2
      tuple  <- fiber.join
    } yield tuple

}
