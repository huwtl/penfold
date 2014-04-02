package org.huwtl.penfold.app.support.json

import org.json4s._
import org.json4s.Extraction._
import org.json4s.jackson.JsonMethods._
import org.huwtl.penfold.domain.event._
import org.json4s.jackson.Serialization
import org.huwtl.penfold.domain.event.JobCreated
import org.huwtl.penfold.domain.event.JobTriggered
import org.json4s.ShortTypeHints

class EventSerializer {
  implicit val formats = Serialization.formats(ShortTypeHints(List(
    classOf[JobCreated],
    classOf[JobTriggered],
    classOf[JobStarted],
    classOf[JobCompleted],
    classOf[JobCancelled]))
  ) +
    new PayloadJsonSerializer +
    new DateTimeJsonSerializer +
    new StatusJsonSerializer +
    new IdJsonSerializer +
    new VersionJsonSerializer +
    new QueueIdJsonSerializer

  def serialize(event: Event) = {
    compact(decompose(event))
  }

  def deserialize(json: String) = {
    parse(json).extract[Event]
  }
}