package org.huwtl.penfold.domain.patch

case class Add(path: String, value: Value) extends PatchOperation {

  override def exec(existing: Map[String, Any]): Map[String, Any] = {

    def applyToMap(pathNames: List[String], value: Value, mapElem: Map[String, Any]): Map[String, Any] = {
      pathNames match {
        case Nil => mapElem
        case (pathName :: remainingPathNames) => {
          mapElem.get(pathName) match {
            case Some(map: Map[_, _]) => mapElem.updated(pathName, applyToMap(remainingPathNames, value, map.asInstanceOf[Map[String, Any]]))
            case Some(list: List[_]) => mapElem.updated(pathName, applyToList(remainingPathNames, value, list))
            case None if remainingPathNames.isEmpty => mapElem.updated(pathName, value.content)
            case None => throw new IllegalStateException(s"Value $pathName does not exist")
            case Some(_) => throw new IllegalStateException(s"Value $pathName already exists, use replace operation instead")
          }
        }
      }
    }

    def applyToList(pathNames: List[String], value: Value, listElem: List[Any]): List[Any] = {
      def insertAt(e: Any, n: Int, ls: List[Any]): List[Any] = ls.splitAt(n) match {
        case (pre, post) => pre ::: e :: post
      }

      pathNames match {
        case Nil => listElem
        case (pathName :: remainingPathNames) => insertAt(value.content, pathName.toInt, listElem)
      }
    }

    applyToMap(pathParts, value, existing)
  }
}

