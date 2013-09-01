package org.huwtl.penfold.app.support

import org.json4s._
import org.huwtl.penfold.domain.Id
import org.json4s.TypeInfo
import org.json4s.JsonAST.JString

class IdJsonSerializer extends Serializer[Id] {
  private val IdClass = classOf[Id]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Id] = {
    case (TypeInfo(IdClass, _), json) => Id(json.extract[String])
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case id: Id => new JString(id.value)
  }
}
