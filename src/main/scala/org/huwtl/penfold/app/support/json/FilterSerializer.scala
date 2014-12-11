package org.huwtl.penfold.app.support.json

import org.json4s._
import org.json4s.Extraction._
import org.json4s.jackson.JsonMethods._
import org.huwtl.penfold.readstore._
import org.huwtl.penfold.readstore.LT
import org.huwtl.penfold.readstore.IN
import org.huwtl.penfold.readstore.GT
import org.huwtl.penfold.readstore.EQ
import org.json4s.ShortTypeHints

class FilterSerializer {
  implicit val formats = new Formats {
    val dateFormat = DefaultFormats.lossless.dateFormat
    override val typeHints = ShortTypeHints(classOf[EQ] :: classOf[LT] :: classOf[GT]:: classOf[IN] :: Nil)
    override val typeHintFieldName = "op"
  } +
    new QueryParamTypeJsonSerializer +
    FieldSerializer[Filter]()

  def serialize(filter: Filter) = {
    compact(decompose(filter))
  }

  def deserialize(json: String) = {
    parse(json).extract[Filter]
  }
}