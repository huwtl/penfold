package org.huwtl.penfold.app.support

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.Extraction._
import org.huwtl.penfold.domain._
import org.json4s.JValue
import org.json4s.TypeInfo
import org.huwtl.penfold.domain.Payload
import org.huwtl.penfold.domain.Job
import org.huwtl.penfold.domain.Cron

class JobJsonConverter {
  implicit val formats = DefaultFormats + new PayloadJsonSerializer + new CronJsonSerializer +
    new DateTimeJsonSerializer + new StatusJsonSerializer + new IdJsonSerializer + new JobTypeJsonSerializer

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
      Cron(json.extract[String])
    }
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case cron: Cron => new JString(cron.toString)
  }
}

private class StatusJsonSerializer extends Serializer[Status] {
  private val StatusClass = classOf[Status]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Status] = {
    case (TypeInfo(StatusClass, _), json) => Status.from(json.extract[String])
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case status: Status => new JString(status.name)
  }
}

private class JobTypeJsonSerializer extends Serializer[JobType] {
  private val JobTypeClass = classOf[JobType]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), JobType] = {
    case (TypeInfo(JobTypeClass, _), json) => JobType(json.extract[String])
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case id: Id => new JString(id.value)
  }
}