package com.qmetric.penfold.app.support.json

import org.json4s._
import org.json4s.TypeInfo
import org.json4s.JsonAST.JString
import com.qmetric.penfold.domain.model.Assignee

class AssigneeJsonSerializer extends Serializer[Assignee] {
  private val AssigneeClass = classOf[Assignee]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Assignee] = {
    case (TypeInfo(AssigneeClass, _), json) => Assignee(json.extract[String])
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case assignee: Assignee => new JString(assignee.username)
  }
}
