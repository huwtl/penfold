package com.hlewis.rabbit_scheduler.job

import net.liftweb.json._
import net.liftweb.json.TypeInfo

class JobSerializer extends Serializer[Job] {
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