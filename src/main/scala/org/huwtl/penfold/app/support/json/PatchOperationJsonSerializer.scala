package org.huwtl.penfold.app.support.json

import org.json4s._
import org.huwtl.penfold.domain.model.patch.PatchOperation
import org.json4s.jackson.JsonMethods._

class PatchOperationJsonSerializer extends Serializer[PatchOperation] {
  private val PatchOperationClass = classOf[PatchOperation]

  private val serializer = new PatchOperationSerializer

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), PatchOperation] = {
    case (TypeInfo(PatchOperationClass, _), json) => serializer.deserialize(compact(json))
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case op: PatchOperation => parse(serializer.serialize(op))
  }
}