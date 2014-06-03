package com.qmetric.penfold.app.support.json

import org.json4s._
import org.json4s.TypeInfo
import com.qmetric.penfold.domain.model.AggregateVersion

class VersionJsonSerializer extends Serializer[AggregateVersion] {
  private val VersionClass = classOf[AggregateVersion]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), AggregateVersion] = {
    case (TypeInfo(VersionClass, _), json) => AggregateVersion(json.extract[Int])
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case version: AggregateVersion => new JInt(version.number)
  }
}
