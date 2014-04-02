package org.huwtl.penfold.app.support.json

import org.specs2.mutable.Specification
import scala.io.Source._
import org.json4s.jackson.JsonMethods._
import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.event._
import org.huwtl.penfold.domain.event.JobCompleted
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.domain.model.BoundQueue
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.event.JobCreated
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.Binding
import org.huwtl.penfold.domain.event.JobTriggered
import org.huwtl.penfold.domain.event.JobStarted
import org.specs2.matcher.DataTables

class EventSerializerTest extends Specification with DataTables {
  val dateTime = new DateTime(2014, 2, 3, 12, 47, 54)
  val queue1 = QueueId("q1")
  val queue2 = QueueId("q2")
  val jobCreatedEvent = JobCreated(AggregateId("a1"), Version.init, dateTime, Binding(List(BoundQueue(queue1))), new DateTime(2014, 2, 3, 14, 30, 1), Payload(Map("stuff" -> "something", "nested" -> Map("inner" -> true))))
  val jobTriggeredEvent = JobTriggered(AggregateId("a1"), Version.init, dateTime, List(queue1, queue2))
  val jobStartedEvent = JobStarted(AggregateId("a1"), Version.init, dateTime, queue1)
  val jobCancelledEvent = JobCancelled(AggregateId("a1"), Version.init, dateTime, List(queue1, queue2))
  val jobCompletedEvent = JobCompleted(AggregateId("a1"), Version.init, dateTime, queue1)
  val serializer = new EventSerializer

  "deserialise job event" in {
    "jsonPath"           || "expectedEvent"   |
    "job_created.json"   !! jobCreatedEvent   |
    "job_triggered.json" !! jobTriggeredEvent |
    "job_started.json"   !! jobStartedEvent   |
    "job_cancelled.json" !! jobCancelledEvent |
    "job_completed.json" !! jobCompletedEvent |> {
      (jsonPath, expectedEvent) =>
        val json = fromInputStream(getClass.getClassLoader.getResourceAsStream(s"fixtures/events/$jsonPath")).mkString
        val actualEvent = serializer.deserialize(json)
        actualEvent must beEqualTo(expectedEvent)
    }
  }

  "serialise job event" in {
    "event"             || "expectedJsonPath"   |
      jobCreatedEvent   !! "job_created.json"   |
      jobTriggeredEvent !! "job_triggered.json" |
      jobStartedEvent   !! "job_started.json"   |
      jobCancelledEvent !! "job_cancelled.json" |
      jobCompletedEvent !! "job_completed.json" |> {
      (event, expectedJsonPath) =>
        val expectedJson = compact(parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(s"fixtures/events/${expectedJsonPath}")).mkString))
        val json = serializer.serialize(event)
        json must beEqualTo(expectedJson)
    }
  }
}
