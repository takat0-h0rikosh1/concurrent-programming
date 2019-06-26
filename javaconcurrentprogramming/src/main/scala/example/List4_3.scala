package example

import apple.laf.JRSUIConstants.Widget

// ステートを private はロックでガードする
class List4_3 {

  case class Widget(value: String)

  class PrivateLock {
    // Java では new Object()
    private val myLock = AnyRef
    // @GuardBy("myLock")
    val widget = Widget("hoge")

    def someMethod() = myLock.synchronized {
      // ウィジェットのステートにアクセスまたは変更する
      ???
    }
  }
}
