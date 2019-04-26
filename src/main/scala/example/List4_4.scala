package example

// モニタを使った車両追跡管理クラス
class List4_4 {

  // MonitorVehicleTracker スレッドセーフ
  // MutablePoint はスレッドセーフでない
  class MonitorVehicleTracker(private final val locations: Map[String, MutablePoint]) {

    def getLocations: Map[String, MutablePoint] = synchronized(deepCopy(locations))

    def getLocation(id: String): MutablePoint = synchronized(locations(id))

    def setLocation(id: String, x: Int, y: Int): Unit = {
      val loc = locations(id)
      loc.x = x
      loc.y = y
    }

    // スレッドセーフを維持する方法の一貫として、可変データのコピーを返す
    private def deepCopy(m: Map[String, MutablePoint]): Map[String, MutablePoint] = {
      m.foldLeft(Map.empty[String, MutablePoint]) {case (result,(id, _)) =>
        result + (id -> MutablePoint(m(id)))
      }
    }
  }

  case class MutablePoint(
                           var x: Int = 0,
                           var y: Int = 0
                         ) {
  }

  object MutablePoint {
    def apply(p: MutablePoint): MutablePoint =
      MutablePoint(p.x, p.y)
  }

}
