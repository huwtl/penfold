package org.huwtl.penfold.app.support.json

import org.json4s._
import org.json4s.Extraction._
import org.json4s.TypeInfo
import org.json4s.JValue
import org.huwtl.penfold.domain.patch.Value

class ValueJsonSerializer extends Serializer[Value] {
  private val ValueClass = classOf[Value]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Value] = {
    case (TypeInfo(ValueClass, _), json) => {
      Value(json.values.asInstanceOf[Any])
    }
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case value: Value => decompose(value.content)
  }
}
