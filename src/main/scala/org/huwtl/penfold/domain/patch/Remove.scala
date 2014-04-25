package org.huwtl.penfold.domain.patch

case class Remove(path: String) extends PatchOperation{
  override def exec(existing: Map[String, Any]): Map[String, Any] = {

    def removeFromMap(pathNames: List[String], mapElem: Map[String, Any]): Map[String, Any] = {
      pathNames match {
        case Nil => mapElem
        case (pathName :: remainingPathNames) => {
          mapElem.get(pathName) match {
            case Some(map: Map[_, _]) => mapElem.updated(pathName, removeFromMap(remainingPathNames, map.asInstanceOf[Map[String, Any]]))
            case Some(list: List[_]) => mapElem.updated(pathName, removeFromList(remainingPathNames, list))
            case Some(_) => mapElem - pathName
            case None => throw new IllegalStateException(s"Value $pathName does not exist")
          }
        }
      }
    }

    def removeFromList(pathNames: List[String], listElem: List[Any]): List[Any] = {
      def removeAt(index: Int, list: List[Any]) = list.patch(index, Nil, 1)

      pathNames match {
        case Nil => listElem
        case (pathName :: remainingPathNames) => removeAt(pathName.toInt, listElem)
      }
    }

    removeFromMap(pathParts, existing)
  }
}
