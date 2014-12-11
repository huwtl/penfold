package org.huwtl.penfold.app.support.json

import org.json4s._
import org.json4s.TypeInfo
import org.json4s.JsonAST.JString
import org.huwtl.penfold.readstore.QueryParamType

class QueryParamTypeJsonSerializer extends Serializer[QueryParamType] {
  private val QueryParamTypeClass = classOf[QueryParamType]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), QueryParamType] = {
    case (TypeInfo(QueryParamTypeClass, _), json) => QueryParamType.from(json.extract[String])
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case paramType: QueryParamType => new JString(paramType.name)
  }
}
