package org.huwtl.penfold.app.support.json

import org.json4s._
import org.huwtl.penfold.domain.model.QueueName
import org.json4s.TypeInfo
import org.json4s.JsonAST.JString

class QueueNameJsonSerializer extends Serializer[QueueName] {
  private val QueueNameClass = classOf[QueueName]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), QueueName] = {
    case (TypeInfo(QueueNameClass, _), json) => QueueName(json.extract[String])
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case queueName: QueueName => new JString(queueName.value)
  }
}
