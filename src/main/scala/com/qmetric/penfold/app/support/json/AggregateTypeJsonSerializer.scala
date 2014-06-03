package com.qmetric.penfold.app.support.json

import org.json4s._
import com.qmetric.penfold.domain.model.AggregateType
import org.json4s.TypeInfo

class AggregateTypeJsonSerializer extends Serializer[AggregateType] {
  private val AggregateTypeClass = classOf[AggregateType]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), AggregateType] = {
    case (TypeInfo(AggregateTypeClass, _), json) => AggregateType.from(json.extract[String]).get
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case aggregateType: AggregateType => new JString(aggregateType.name)
  }
}
