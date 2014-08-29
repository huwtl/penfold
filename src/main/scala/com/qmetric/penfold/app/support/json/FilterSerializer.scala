package com.qmetric.penfold.app.support.json

import org.json4s._
import org.json4s.Extraction._
import org.json4s.jackson.JsonMethods._
import com.qmetric.penfold.readstore._
import com.qmetric.penfold.readstore.LessThan
import com.qmetric.penfold.readstore.In
import com.qmetric.penfold.readstore.GreaterThan
import com.qmetric.penfold.readstore.Equals
import org.json4s.ShortTypeHints
import com.qmetric.penfold.domain.event.Event

class FilterSerializer {
  implicit val formats = new Formats {
    val dateFormat = DefaultFormats.lossless.dateFormat
    override val typeHints = ShortTypeHints(classOf[Equals] :: classOf[LessThan] :: classOf[GreaterThan]:: classOf[In] :: Nil)
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