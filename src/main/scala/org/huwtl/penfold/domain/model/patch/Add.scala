package org.huwtl.penfold.domain.model.patch

case class Add(path: String, value: Value) extends PatchOperation {

  override def exec(existing: Map[String, Any]): Map[String, Any] = {

    def applyToMap(pathNames: List[String], value: Value, mapElems: Map[String, Any]): Map[String, Any] = {
      pathNames match {
        case Nil => mapElems
        case (pathName :: remainingPathNames) => {
          mapElems.get(pathName) match {
            case None if remainingPathNames.isEmpty => mapElems.updated(pathName, value.content)
            case None if remainingPathNames.nonEmpty => throw new IllegalArgumentException(s"Value $pathName does not exist")
            case Some(_) if remainingPathNames.isEmpty => throw new IllegalArgumentException(s"Value $pathName already exists, use replace operation instead")
            case Some(map: Map[_, _]) => mapElems.updated(pathName, applyToMap(remainingPathNames, value, map.asInstanceOf[Map[String, Any]]))
            case Some(list: List[_]) => mapElems.updated(pathName, applyToList(remainingPathNames, value, list))
          }
        }
      }
    }

    def applyToList(pathNames: List[String], value: Value, listElems: List[Any]): List[Any] = {
      def insertAt(value: Any, index: Int, list: List[Any]) = list.patch(index, List(value), 0)

      pathNames match {
        case Nil => Nil
        case (pathName :: Nil) => insertAt(value.content, pathName.toInt, listElems)
        case (pathName :: remainingPathNames) => {
          listElems(pathName.toInt) match {
            case map: Map[_, _] => listElems.updated(pathName.toInt, applyToMap(remainingPathNames, value, map.asInstanceOf[Map[String, Any]]))
            case list: List[_] => listElems.updated(pathName.toInt, applyToList(remainingPathNames, value, list))
            case _ => throw new IllegalArgumentException(s"Value ${remainingPathNames.head} does not exist")
          }
        }
      }
    }

    applyToMap(pathParts, value, existing)
  }
}

