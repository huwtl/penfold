package org.huwtl.penfold.app.support.json

import org.json4s._
import org.json4s.Extraction._
import org.json4s.jackson.JsonMethods._
import org.huwtl.penfold.domain.event._
import org.huwtl.penfold.domain.event.TaskCreated
import org.huwtl.penfold.domain.event.TaskTriggered
import org.json4s.ShortTypeHints

class EventSerializer {

  implicit val formats = new Formats {
    val dateFormat = DefaultFormats.lossless.dateFormat
    override val typeHints = ShortTypeHints(classOf[TaskCreated] :: classOf[FutureTaskCreated] :: classOf[TaskPayloadUpdated] :: classOf[TaskTriggered] :: classOf[TaskStarted] :: classOf[TaskRequeued] :: classOf[TaskCompleted] :: classOf[TaskCancelled] :: Nil)
    override val typeHintFieldName = "type"
  } +
    new PayloadJsonSerializer +
    new DateTimeJsonSerializer +
    new StatusJsonSerializer +
    new AggregateTypeJsonSerializer +
    new IdJsonSerializer +
    new VersionJsonSerializer +
    new QueueIdJsonSerializer +
    new PatchOperationJsonSerializer +
    new ValueJsonSerializer +
    FieldSerializer[Event]()

  def serialize(event: Event) = {
    compact(decompose(event))
  }

  def deserialize(json: String) = {
    parse(json).extract[Event]
  }
}