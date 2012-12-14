package com.hlewis.rabbit_scheduler.app

import net.liftweb.json._
import com.hlewis.rabbit_scheduler.domain.Body
import com.hlewis.rabbit_scheduler.domain.Header
import net.liftweb.json.TypeInfo
import com.hlewis.rabbit_scheduler.domain.Job

class JobJsonSerializer extends Serializer[Job] {
  private val JobClass = classOf[Job]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Job] = {
    case (TypeInfo(JobClass, _), json) => {
      val header = (json \ "header").extract[Header]
      val body = (json \ "body").values

      Job(header, Body(body.asInstanceOf[Map[String, Any]]))
    }
  }

  def serialize(implicit format: Formats) = null
}