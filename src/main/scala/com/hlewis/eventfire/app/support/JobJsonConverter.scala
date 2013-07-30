package com.hlewis.eventfire.app.support

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.Extraction._
import com.hlewis.eventfire.domain.{Cron, Payload, Job}
import org.json4s.JValue
import org.json4s.TypeInfo

class JobJsonConverter {
  implicit val formats = DefaultFormats + new PayloadJsonSerializer + new CronJsonSerializer + new DateTimeJsonSerializer

  def jsonFrom(job: Job) = {
    pretty(decompose(job))
  }

  def jobFrom(json: String) = {
    parse(json).extract[Job]
  }

  def jsonFrom(payload: Payload) = {
    pretty(decompose(payload))
  }

  def jobPayloadFrom(json: String) = {
    parse(json).extract[Payload]
  }
}

private class PayloadJsonSerializer extends Serializer[Payload] {
  private val PayloadClass = classOf[Payload]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Payload] = {
    case (TypeInfo(PayloadClass, _), json) => Payload(json.values.asInstanceOf[Map[String, Any]])
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case payload: Payload => decompose(payload.content)
  }
}

private class CronJsonSerializer extends Serializer[Cron] {
  private val CronClass = classOf[Cron]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Cron] = {
    case (TypeInfo(CronClass, _), json) => {
      val cronStr = json.extract[String]
      val cronParts = cronStr.split(' ')

      Cron(cronParts(0), cronParts(1), cronParts(2), cronParts(3), cronParts(4), cronParts(5), cronParts(6))
    }
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case cron: Cron => new JString(cron.toString)
  }
}