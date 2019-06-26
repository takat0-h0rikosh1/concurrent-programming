package example

import scala.collection.immutable.HashSet

// 拘束を使ってスレッドセーフ性を確保する
class List4_2 {

  case class Person(name: String)

  class PersonSet {

    private final val mySet: Set[Person] = new HashSet[Person]

    //複数スレッドから自分のインスタンスの add が呼ばれた場合に排他され、
    //一度に1スレッド分しか実行されない
    def add(p: Person) = synchronized{
      mySet + p
    }

    //複数スレッドから自分のインスタンスの contains が呼ばれた場合に排他され、
    //一度に1スレッド分しか実行されない
    def contains(p: Person) = synchronized {
      mySet.contains(p)
    }

    // ↑ 同じ
    // def contains(p: Person) = this.synchronized {
    //   mySet.contains(p)
    // }
  }

}
