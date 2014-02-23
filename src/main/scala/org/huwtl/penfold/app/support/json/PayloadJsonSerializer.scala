package org.huwtl.penfold.app.support.json

import org.json4s._
import org.json4s.Extraction._
import org.huwtl.penfold.domain.model.Payload
import org.json4s.TypeInfo

class PayloadJsonSerializer extends Serializer[Payload] {
  private val PayloadClass = classOf[Payload]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Payload] = {
    case (TypeInfo(PayloadClass, _), json) => Payload(json.values.asInstanceOf[Map[String, Any]])
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case payload: Payload => decompose(payload.content)
  }
}
