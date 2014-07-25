package com.qmetric.penfold.app.support.json

import org.specs2.mutable.Specification
import scala.io.Source._
import org.json4s.jackson.JsonMethods._
import com.qmetric.penfold.domain.model.Payload
import org.specs2.matcher.DataTables
import com.qmetric.penfold.domain.model.patch.{Patch, Value, Add}
import com.qmetric.penfold.support.TestModel

class EventSerializerTest extends Specification with DataTables {
  val payload = Payload(Map("stuff" -> "something", "nested" -> Map("inner" -> true)))
  val payloadUpdate = Patch(List(Add("/a/b", Value("1"))))
  val taskCreatedEvent = TestModel.events.createdEvent.copy(payload = payload)
  val futureTaskCreatedEvent = TestModel.events.futureCreatedEvent.copy(payload = payload)
  val taskPayloadUpdatedEvent = TestModel.events.payloadUpdatedEvent.copy(payloadUpdate = payloadUpdate, score = Some(100))
  val taskTriggeredEvent = TestModel.events.triggeredEvent
  val taskStartedEvent = TestModel.events.startedEvent
  val taskUnassignedEvent = TestModel.events.unassignedEvent
  val taskRequeuedEvent = TestModel.events.requeuedEvent
  val taskRescheduledEvent = TestModel.events.rescheduledEvent
  val taskClosedEvent = TestModel.events.closedEvent
  val taskArchivedEvent = TestModel.events.archivedEvent
  val serializer = new EventSerializer

  "deserialise task event" in {
    "jsonPath"                  || "expectedEvent"         |
    "task_created.json"         !! taskCreatedEvent        |
    "future_task_created.json"  !! futureTaskCreatedEvent  |
    "task_payload_updated.json" !! taskPayloadUpdatedEvent |
    "task_triggered.json"       !! taskTriggeredEvent      |
    "task_started.json"         !! taskStartedEvent        |
    "task_unassigned.json"      !! taskUnassignedEvent     |
    "task_requeued.json"        !! taskRequeuedEvent       |
    "task_rescheduled.json"     !! taskRescheduledEvent    |
    "task_closed.json"          !! taskClosedEvent         |
    "task_archived.json"        !! taskArchivedEvent       |> {
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
    taskUnassignedEvent     !! "task_unassigned.json"      |
    taskRequeuedEvent       !! "task_requeued.json"        |
    taskRescheduledEvent    !! "task_rescheduled.json"     |
    taskClosedEvent         !! "task_closed.json"          |
    taskArchivedEvent       !! "task_archived.json"        |> {
      (event, expectedJsonPath) =>
        val expectedJson = compact(parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(s"fixtures/events/${expectedJsonPath}")).mkString))
        val json = serializer.serialize(event)
        json must beEqualTo(expectedJson)
    }
  }
}
