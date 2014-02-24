package org.huwtl.penfold.app.support.json

import org.json4s._
import org.json4s.Extraction._
import org.json4s.jackson.JsonMethods._

class ObjectSerializer {
  implicit val formats = DefaultFormats +
    new PayloadJsonSerializer +
    new DateTimeJsonSerializer +
    new StatusJsonSerializer +
    new IdJsonSerializer +
    new VersionJsonSerializer +
    new QueueNameJsonSerializer

  def serialize[T](event: T) = {
    pretty(decompose(event))
  }

  def deserialize[T](json: String)(implicit manifest: Manifest[T]) = {
    parse(json).extract[T]
  }
}