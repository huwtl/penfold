package com.hlewis.eventfire.app.support

import net.liftweb.json._
import com.hlewis.eventfire.domain.{Cron, Body, Header, Job}
import net.liftweb.json.TypeInfo

class JobJsonSerializer extends Serializer[Job] {
  private val JobClass = classOf[Job]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Job] = {
    case (TypeInfo(JobClass, _), json) => {
      val header = (json \ "header").extract[Header]
      val body = (json \ "body").values

      Job(header, Body(body.asInstanceOf[Map[String, Any]]))
    }
  }

  def serialize(implicit format: Formats) = null
}