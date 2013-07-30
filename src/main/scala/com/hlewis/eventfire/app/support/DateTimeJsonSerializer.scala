package com.hlewis.eventfire.app.support

import org.json4s._
import org.json4s.JValue
import org.json4s.TypeInfo
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

private class DateTimeJsonSerializer extends Serializer[DateTime] {
  private val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  private val DateTimeClass = classOf[DateTime]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), DateTime] = {
    case (TypeInfo(DateTimeClass, _), json) => {
      dateFormatter.parseDateTime(json.extract[String])
    }
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case dateTime: DateTime => new JString(dateFormatter.print(dateTime))
  }
}