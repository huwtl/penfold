package com.qmetric.penfold.app.support.json

import org.json4s._
import com.qmetric.penfold.domain.model.CloseResultType
import org.json4s.TypeInfo
import org.json4s.JsonAST.JString

class CloseResultTypeJsonSerializer extends Serializer[CloseResultType] {
  private val CloseResultTypeClass = classOf[CloseResultType]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), CloseResultType] = {
    case (TypeInfo(CloseResultTypeClass, _), json) => CloseResultType.from(json.extract[String]).get
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case resultType: CloseResultType => new JString(resultType.name)
  }
}
