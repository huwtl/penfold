package org.huwtl.penfold.app.support.json

import org.json4s._
import org.huwtl.penfold.domain.model.Status
import org.json4s.TypeInfo
import org.json4s.JsonAST.JString

class StatusJsonSerializer extends Serializer[Status] {
  private val StatusClass = classOf[Status]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Status] = {
    case (TypeInfo(StatusClass, _), json) => Status.from(json.extract[String]).get
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case status: Status => new JString(status.name)
  }
}
