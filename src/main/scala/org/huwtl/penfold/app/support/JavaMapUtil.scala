package org.huwtl.penfold.app.support

import scala.collection.JavaConversions._

object JavaMapUtil {
  def deepConvertToJavaMap(scalaMap: Map[String, Any]): java.util.Map[String, Any] = {
    def deepConvertToJavaIterable(scalaList: Iterable[Any]): Iterable[Any] = {
      scalaList.map {
        case innerMap: Map[_, _] => deepConvertToJavaMap(innerMap.asInstanceOf[Map[String, Any]])
        case innerIterable: Iterable[_] => asJavaIterable(deepConvertToJavaIterable(innerIterable.asInstanceOf[List[Any]]))
        case notList => notList
      }
    }

    scalaMap.map {
      case (key, innerMap: Map[_, _]) => (key, mapAsJavaMap(deepConvertToJavaMap(innerMap.asInstanceOf[Map[String, Any]])))
      case (key, innerIterable: Iterable[_]) => (key, asJavaCollection(deepConvertToJavaIterable(innerIterable)))
      case notMap => notMap
    }
  }
}
