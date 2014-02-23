package org.huwtl.penfold.app.support.json

import org.json4s._
import org.huwtl.penfold.domain.model.JobType
import org.json4s.TypeInfo
import org.json4s.JsonAST.JString

class JobTypeJsonSerializer extends Serializer[JobType] {
  private val JobTypeClass = classOf[JobType]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), JobType] = {
    case (TypeInfo(JobTypeClass, _), json) => JobType(json.extract[String])
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case jobType: JobType => new JString(jobType.value)
  }
}
