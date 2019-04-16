package example

object Hello extends Greeting with App {
  @volatile var aslAeep: Boolean  = true
  println(greeting)
}

trait Greeting {
  lazy val greeting: String = "hello"
}

//object ConcurrentExecutor {
//  def execute()
//}
