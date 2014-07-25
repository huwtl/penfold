package com.qmetric.penfold.app.support.json

import org.json4s._
import org.json4s.TypeInfo
import org.json4s.JsonAST.JString
import com.qmetric.penfold.domain.model.User

class UserJsonSerializer extends Serializer[User] {
  private val UserClass = classOf[User]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), User] = {
    case (TypeInfo(UserClass, _), json) => User(json.extract[String])
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case user: User => new JString(user.username)
  }
}
