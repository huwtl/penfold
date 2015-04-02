package org.huwtl.penfold.app.support.json

import org.json4s._
import org.json4s.Extraction._
import org.json4s.jackson.JsonMethods._
import org.huwtl.penfold.readstore.Filter

class ObjectSerializer {
  implicit val formats = DefaultFormats +
    new PayloadJsonSerializer +
    new DateTimeJsonSerializer +
    new StatusJsonSerializer +
    new CloseResultTypeJsonSerializer +
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