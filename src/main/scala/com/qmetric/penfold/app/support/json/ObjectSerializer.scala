package com.qmetric.penfold.app.support.json

import org.json4s._
import org.json4s.Extraction._
import org.json4s.jackson.JsonMethods._
import com.qmetric.penfold.readstore.Filter

class ObjectSerializer {
  implicit val formats = DefaultFormats +
    new PayloadJsonSerializer +
    new DateTimeJsonSerializer +
    new StatusJsonSerializer +
    new AggregateTypeJsonSerializer +
    new IdJsonSerializer +
    new VersionJsonSerializer +
    new QueueIdJsonSerializer +
    new PatchOperationJsonSerializer +
    new ValueJsonSerializer +
    new UserJsonSerializer +
    new FilterJsonSerializer +
    FieldSerializer[Filter]()

  def serialize[T](obj: T) = {
    compact(decompose(obj))
  }

  def deserialize[T](json: String)(implicit manifest: Manifest[T]) = {
    parse(json).extract[T]
  }
}