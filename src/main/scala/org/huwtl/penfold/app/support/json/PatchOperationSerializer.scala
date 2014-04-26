package org.huwtl.penfold.app.support.json

import org.json4s._
import org.json4s.Extraction._
import org.json4s.jackson.JsonMethods._
import org.json4s.ShortTypeHints
import org.huwtl.penfold.domain.model.patch.{PatchOperation, Replace, Remove, Add}

class PatchOperationSerializer {
  implicit val formats = new Formats {
    val dateFormat = DefaultFormats.lossless.dateFormat
    override val typeHints = ShortTypeHints(classOf[Add] :: classOf[Remove] :: classOf[Replace]:: Nil)
    override val typeHintFieldName = "op"
  } + new ValueJsonSerializer

  def serialize(patchOperation: PatchOperation) = {
    compact(decompose(patchOperation))
  }

  def deserialize(json: String) = {
    parse(json).extract[PatchOperation]
  }
}