package com.qmetric.penfold.app.support.json

import org.json4s._
import com.qmetric.penfold.domain.model.QueueId
import org.json4s.TypeInfo
import org.json4s.JsonAST.JString

class QueueIdJsonSerializer extends Serializer[QueueId] {
  private val QueueIdClass = classOf[QueueId]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), QueueId] = {
    case (TypeInfo(QueueIdClass, _), json) => QueueId(json.extract[String])
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case queueId: QueueId => new JString(queueId.value)
  }
}
