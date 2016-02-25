package com.qmetric.penfold.app.support.json

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneId, ZonedDateTime}

import org.joda.time.DateTime
import org.json4s.{JValue, TypeInfo, _}

private class DateTimeJsonSerializer extends Serializer[DateTime] {
  private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  private val DateTimeClass = classOf[DateTime]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), DateTime] = {
    case (TypeInfo(DateTimeClass, _), json) =>
      val parseLocalDateTime = LocalDateTime.parse(json.extract[String], dateFormatter)
      val atZone: ZonedDateTime = parseLocalDateTime.atZone(ZoneId.systemDefault())
      new DateTime(atZone.toInstant.toEpochMilli)
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case dateTime: DateTime => new JString(dateFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime.getMillis), ZoneId.systemDefault())))
  }
}