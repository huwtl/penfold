package com.qmetric.penfold.app.support.json

import org.json4s._
import org.json4s.Extraction._
import org.json4s.jackson.JsonMethods._
import com.qmetric.penfold.readstore._
import com.qmetric.penfold.readstore.LT
import com.qmetric.penfold.readstore.IN
import com.qmetric.penfold.readstore.GT
import com.qmetric.penfold.readstore.EQ
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