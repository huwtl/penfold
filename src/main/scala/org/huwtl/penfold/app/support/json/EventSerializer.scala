package org.huwtl.penfold.app.support.json

import org.json4s._
import org.json4s.Extraction._
import org.json4s.jackson.JsonMethods._
import org.huwtl.penfold.domain.event._
import org.huwtl.penfold.domain.event.JobCreated
import org.huwtl.penfold.domain.event.JobTriggered
import org.json4s.ShortTypeHints

class EventSerializer {

  implicit val formats = new Formats {
    val dateFormat = DefaultFormats.lossless.dateFormat
    override val typeHints = ShortTypeHints(classOf[JobCreated] :: classOf[FutureJobCreated] :: classOf[JobTriggered] :: classOf[JobStarted] :: classOf[JobCompleted] :: classOf[JobCancelled] :: Nil)
    override val typeHintFieldName = "type"
  } +
    new PayloadJsonSerializer +
    new DateTimeJsonSerializer +
    new StatusJsonSerializer +
    new AggregateTypeJsonSerializer +
    new IdJsonSerializer +
    new VersionJsonSerializer +
    new QueueIdJsonSerializer +
    FieldSerializer[Event]()

  def serialize(event: Event) = {
    compact(decompose(event))
  }

  def deserialize(json: String) = {
    parse(json).extract[Event]
  }
}