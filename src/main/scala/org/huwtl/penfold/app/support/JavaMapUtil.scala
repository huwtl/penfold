package org.huwtl.penfold.app.support

import scala.collection.JavaConversions._
import java.util

object JavaMapUtil {
  def deepConvertToJavaMap(scalaMap: Map[String, Any]): java.util.Map[String, Any] = {
    def deepConvertToJavaIterable(scalaList: Iterable[Any]): Iterable[Any] = {
      scalaList.map {
        case innerMap: Map[_, _] => deepConvertToJavaMap(innerMap.asInstanceOf[Map[String, Any]])
        case innerIterable: Iterable[_] => new util.ArrayList(asJavaCollection(deepConvertToJavaIterable(innerIterable)))
        case notList => notList
      }
    }

    scalaMap.map {
      case (key, innerMap: Map[_, _]) => (key, mapAsJavaMap(deepConvertToJavaMap(innerMap.asInstanceOf[Map[String, Any]])))
      case (key, innerIterable: Iterable[_]) => (key, new util.ArrayList(asJavaCollection(deepConvertToJavaIterable(innerIterable))))
      case notMap => notMap
    }
  }
}
