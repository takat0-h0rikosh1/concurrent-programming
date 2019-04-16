package example

/**
  * モニタパターンを使ったシンプルでスレッドセーフなカウンター
  */
class List4_1 {

  final class Counter {

    private var value = 0

    def getValue(): Int = synchronized {
      value
    }

    def increment() = synchronized {
      if (value == Long.MaxValue)
        throw new IllegalStateException("counter overflow")
      value += 1
    }
  }

  case class MyCounter(var value: Int) {

    def getValue(): Int = synchronized {
      value
    }

    def increment() = synchronized {
      if (value == Long.MaxValue)
        throw new IllegalStateException("counter overflow")
      value += 1
    }

  }

}
