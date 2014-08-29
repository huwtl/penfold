package com.qmetric.penfold.app.support.json

import org.json4s._
import org.json4s.jackson.JsonMethods._
import com.qmetric.penfold.readstore.Filter

class FilterJsonSerializer extends Serializer[Filter] {
  private val FilterClass = classOf[Filter]

  private val serializer = new FilterSerializer

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Filter] = {
    case (TypeInfo(FilterClass, _), json) => serializer.deserialize(compact(json))
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case filter: Filter => parse(serializer.serialize(filter))
  }
}