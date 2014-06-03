package com.qmetric.penfold.domain.model.patch

case class Remove(path: String) extends PatchOperation {
  override def exec(existing: Map[String, Any]): Map[String, Any] = {

    def removeFromMap(pathNames: List[String], mapElems: Map[String, Any]): Map[String, Any] = {
      pathNames match {
        case Nil => mapElems
        case (pathName :: remainingPathNames) => {
          mapElems.get(pathName) match {
            case None => mapElems
            case Some(_) if remainingPathNames.isEmpty => mapElems - pathName
            case Some(map: Map[_, _]) => mapElems.updated(pathName, removeFromMap(remainingPathNames, map.asInstanceOf[Map[String, Any]]))
            case Some(list: List[_]) => mapElems.updated(pathName, removeFromList(remainingPathNames, list))
          }
        }
      }
    }

    def removeFromList(pathNames: List[String], listElems: List[Any]): List[Any] = {
      def removeAt(index: Int, list: List[Any]) = list.patch(index, Nil, 1)

      pathNames match {
        case (pathName :: Nil) => removeAt(pathName.toInt, listElems)
        case (pathName :: remainingPathNames) => {
          listElems(pathName.toInt) match {
            case map: Map[_, _] => listElems.updated(pathName.toInt, removeFromMap(remainingPathNames, map.asInstanceOf[Map[String, Any]]))
            case list: List[_] => listElems.updated(pathName.toInt, removeFromList(remainingPathNames, list))
            case _ => listElems
          }
        }
      }
    }

    removeFromMap(pathParts, existing)
  }
}
