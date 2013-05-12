package com.hlewis.eventfire.app.support

import net.liftweb.json._
import net.liftweb.json.TypeInfo
import com.hlewis.eventfire.domain.Cron
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JValue

class CronJsonSerializer extends Serializer[Cron] {
  private val CronClass = classOf[Cron]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Cron] = {
    case (TypeInfo(CronClass, _), json) => {
      val cronStr = json.extract[String]
      val cronParts = cronStr.split(' ')

      Cron(cronParts(0), cronParts(1), cronParts(2), cronParts(3), cronParts(4))
    }
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case cron => new JString(cron.toString)
  }
}