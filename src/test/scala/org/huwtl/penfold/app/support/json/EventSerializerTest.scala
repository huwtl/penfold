package org.huwtl.penfold.app.support.json

import org.specs2.mutable.Specification
import scala.io.Source._
import org.json4s.jackson.JsonMethods._
import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.event._
import org.huwtl.penfold.domain.event.TaskCompleted
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.domain.model.QueueBinding
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.event.TaskCreated
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.event.TaskTriggered
import org.huwtl.penfold.domain.event.TaskStarted
import org.specs2.matcher.DataTables

class EventSerializerTest extends Specification with DataTables {
  val dateTime = new DateTime(2014, 2, 3, 12, 47, 54)
  val queue1 = QueueId("q1")
  val queue2 = QueueId("q2")
  val triggerDate = new DateTime(2014, 2, 3, 14, 30, 1)
  val taskCreatedEvent = TaskCreated(AggregateId("a1"), AggregateVersion.init, dateTime, QueueBinding(queue1), triggerDate, Payload(Map("stuff" -> "something", "nested" -> Map("inner" -> true))), triggerDate.getMillis)
  val futureTaskCreatedEvent = FutureTaskCreated(AggregateId("a1"), AggregateVersion.init, dateTime, QueueBinding(queue1), triggerDate, Payload(Map("stuff" -> "something", "nested" -> Map("inner" -> true))), triggerDate.getMillis)
  val taskPayloadUpdatedEvent = TaskPayloadUpdated(AggregateId("a1"), AggregateVersion.init, dateTime, Payload(Map("stuff" -> "something")), Some("update_type_1"), Some(100))
  val taskTriggeredEvent = TaskTriggered(AggregateId("a1"), AggregateVersion.init, dateTime)
  val taskStartedEvent = TaskStarted(AggregateId("a1"), AggregateVersion.init, dateTime)
  val taskCancelledEvent = TaskCancelled(AggregateId("a1"), AggregateVersion.init, dateTime)
  val taskCompletedEvent = TaskCompleted(AggregateId("a1"), AggregateVersion.init, dateTime)
  val serializer = new EventSerializer

  "deserialise task event" in {
    "jsonPath"                  || "expectedEvent"         |
    "task_created.json"         !! taskCreatedEvent        |
    "future_task_created.json"  !! futureTaskCreatedEvent  |
    "task_payload_updated.json" !! taskPayloadUpdatedEvent |
    "task_triggered.json"       !! taskTriggeredEvent      |
    "task_started.json"         !! taskStartedEvent        |
    "task_cancelled.json"       !! taskCancelledEvent      |
    "task_completed.json"       !! taskCompletedEvent      |> {
      (jsonPath, expectedEvent) =>
        val json = fromInputStream(getClass.getClassLoader.getResourceAsStream(s"fixtures/events/$jsonPath")).mkString
        val actualEvent = serializer.deserialize(json)
        actualEvent must beEqualTo(expectedEvent)
    }
  }

  "serialise task event" in {
    "event"                 || "expectedJsonPath"          |
    taskCreatedEvent        !! "task_created.json"         |
    futureTaskCreatedEvent  !! "future_task_created.json"  |
    taskPayloadUpdatedEvent !! "task_payload_updated.json" |
    taskTriggeredEvent      !! "task_triggered.json"       |
    taskStartedEvent        !! "task_started.json"         |
    taskCancelledEvent      !! "task_cancelled.json"       |
    taskCompletedEvent      !! "task_completed.json"       |> {
      (event, expectedJsonPath) =>
        val expectedJson = compact(parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(s"fixtures/events/${expectedJsonPath}")).mkString))
        val json = serializer.serialize(event)
        json must beEqualTo(expectedJson)
    }
  }
}
