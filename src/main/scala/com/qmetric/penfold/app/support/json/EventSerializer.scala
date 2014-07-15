package com.qmetric.penfold.app.support.json

import org.json4s._
import org.json4s.Extraction._
import org.json4s.jackson.JsonMethods._
import com.qmetric.penfold.domain.event._
import com.qmetric.penfold.domain.event.TaskCreated
import com.qmetric.penfold.domain.event.TaskTriggered
import org.json4s.ShortTypeHints

class EventSerializer {

  implicit val formats = new Formats {
    val dateFormat = DefaultFormats.lossless.dateFormat
    override val typeHints = ShortTypeHints(classOf[TaskCreated] :: classOf[FutureTaskCreated] :: classOf[TaskPayloadUpdated] :: classOf[TaskTriggered] :: classOf[TaskStarted] :: classOf[TaskRequeued] :: classOf[TaskRescheduled] :: classOf[TaskClosed] :: classOf[TaskArchived] :: Nil)
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
    new AssigneeJsonSerializer +
    new UserJsonSerializer +
    FieldSerializer[Event]()

  def serialize(event: Event) = {
    compact(decompose(event))
  }

  def deserialize(json: String) = {
    parse(json).extract[Event]
  }
}