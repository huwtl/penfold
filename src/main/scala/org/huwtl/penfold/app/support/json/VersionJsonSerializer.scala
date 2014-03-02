package org.huwtl.penfold.app.support.json

import org.json4s._
import org.json4s.TypeInfo
import org.huwtl.penfold.domain.model.Version

class VersionJsonSerializer extends Serializer[Version] {
  private val VersionClass = classOf[Version]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Version] = {
    case (TypeInfo(VersionClass, _), json) => Version(json.extract[Int])
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case version: Version => new JInt(version.number)
  }
}
