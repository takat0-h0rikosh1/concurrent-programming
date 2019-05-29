package example

import java.net.ServerSocket
import java.util.concurrent.{Executor, Executors}

object TaskExecutionWebServer {

  private final lazy val NThreads: Int = 100
  private final lazy val exec: Executor = Executors.newFixedThreadPool(NThreads)

  def main(args: Array[String]): Unit = {
    val socket = new ServerSocket(80)
    while (true) {
      socket.accept()
      val task = new Runnable {
        override def run(): Unit = {
          println("Hello World!!!")
        }
      }
      exec.execute(task)
    }
  }

}

object SingleThreadWebServer {
  def main(args: Array[String]): Unit = {
    val socket = new ServerSocket(80)
    while (true) {
      socket.accept()
      println("Hello SingleThreadWebServer!!!")
    }
  }
}

object ThreadPerTaskWebServer {
  def main(args: Array[String]): Unit = {
    val socket = new ServerSocket(80)
    while (true) {
      socket.accept()
      val task = new Runnable {
        override def run(): Unit =
          println("Hello ThreadPerTaskWebServer!!!")
      }
      new Thread(task).start()
    }
  }

}
