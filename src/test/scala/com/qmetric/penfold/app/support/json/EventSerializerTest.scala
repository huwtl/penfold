package com.qmetric.penfold.app.support.json

import com.qmetric.penfold.domain.model.Payload
import com.qmetric.penfold.domain.model.patch.{Patch, Value, Add}
import com.qmetric.penfold.support.{JsonFixtures, TestModel}
import org.specs2.matcher.DataTables
import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EventSerializerTest extends Specification with DataTables with JsonFixtures {
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
  val taskCancelledEvent = TestModel.events.cancelEvent
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
      "task_cancelled.json"       !! taskCancelledEvent      |
      "task_archived.json"        !! taskArchivedEvent       |> {
      (jsonPath, expectedEvent) =>
        val json = jsonFixtureAsString(s"fixtures/events/$jsonPath")
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
      taskCancelledEvent      !! "task_cancelled.json"          |
      taskArchivedEvent       !! "task_archived.json"        |> {
      (event, expectedJsonPath) =>
        val expectedJson = jsonFixture(s"fixtures/events/$expectedJsonPath")
        val json = asJson(serializer.serialize(event))
        json must beEqualTo(expectedJson)
    }
  }
}