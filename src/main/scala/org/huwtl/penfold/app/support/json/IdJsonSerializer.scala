package org.huwtl.penfold.app.support.json

import org.json4s._
import org.json4s.TypeInfo
import org.json4s.JsonAST.JString
import org.huwtl.penfold.domain.model.AggregateId

class IdJsonSerializer extends Serializer[AggregateId] {
  private val AggregateIdClass = classOf[AggregateId]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), AggregateId] = {
    case (TypeInfo(AggregateIdClass, _), json) => AggregateId(json.extract[String])
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case id: AggregateId => new JString(id.value)
  }
}
