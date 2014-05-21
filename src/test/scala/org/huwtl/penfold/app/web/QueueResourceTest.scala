package org.huwtl.penfold.app.web

import org.huwtl.penfold.app.support.hal.{HalQueueFormatter, HalTaskFormatter}
import java.net.URI
import org.json4s.jackson.JsonMethods._
import scala.io.Source._
import org.scalatra.test.specs2.MutableScalatraSpec
import org.joda.time.DateTime
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.readstore._
import org.huwtl.penfold.command.CommandDispatcher
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.readstore.Filter
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.model.AggregateId
import scala.Some
import org.huwtl.penfold.readstore.PageRequest
import org.huwtl.penfold.readstore.TaskRecord
import org.huwtl.penfold.readstore.PageResult
import org.huwtl.penfold.app.AuthenticationCredentials

class QueueResourceTest extends MutableScalatraSpec with Mockito with WebAuthSpecification {
  sequential

  val queueId = QueueId("abc")

  val payload = Payload.empty

  val created = new DateTime(2014, 2, 25, 13, 0, 0, 0)

  val triggerDate = new DateTime(2014, 2, 25, 14, 0, 0, 0)
  
  val sort = triggerDate.getMillis

  val pageSize = 5

  val validCredentials = AuthenticationCredentials("user", "secret")

  val readStore = mock[ReadStore]

  val commandDispatcher = mock[CommandDispatcher]

  addServlet(new QueueResource(readStore, commandDispatcher, new ObjectSerializer, new HalQueueFormatter(new URI("http://host/queues"), new HalTaskFormatter(new URI("http://host/tasks"), new URI("http://host/queues"))), pageSize, Some(validCredentials)), "/queues/*")

  "return 200 with hal+json formatted queue response" in {
    val expectedTask1 = TaskRecord(AggregateId("1"), AggregateVersion.init, created, QueueBinding(queueId), Status.Ready, created, None, None, triggerDate, triggerDate.getMillis, sort, payload)
    val expectedTask2 = TaskRecord(AggregateId("2"), AggregateVersion.init, created, QueueBinding(queueId), Status.Ready, created, None, None, triggerDate, triggerDate.getMillis, sort, payload)
    readStore.retrieveByQueue(queueId, Status.Ready, PageRequest(pageSize), Filters.empty) returns PageResult(List(expectedTask2, expectedTask1), None, None)

    get("/queues/abc/ready", headers = validAuthHeader) {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedQueue.json"))
    }
  }

  "return 200 with hal+json formatted filtered queue response" in {
    val expectedTask1 = TaskRecord(AggregateId("1"), AggregateVersion.init, created, QueueBinding(queueId), Status.Ready, created, None, None, triggerDate, triggerDate.getMillis, sort, payload)
    val expectedTask2 = TaskRecord(AggregateId("2"), AggregateVersion.init, created, QueueBinding(queueId), Status.Ready, created, None, None, triggerDate, triggerDate.getMillis, sort, payload)
    val filters = Filters(List(Filter("data", Some("value"))))
    readStore.retrieveByQueue(queueId, Status.Ready, PageRequest(pageSize), filters) returns PageResult(List(expectedTask2, expectedTask1), None, None)

    get("/queues/abc/ready?_data=value", headers = validAuthHeader) {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredQueue.json"))
    }
  }

  "return 200 with hal+json formatted filtered queue response with multparams" in {
    val expectedTask1 = TaskRecord(AggregateId("1"), AggregateVersion.init, created, QueueBinding(queueId), Status.Ready, created, None, None, triggerDate, triggerDate.getMillis, sort, payload)
    val expectedTask2 = TaskRecord(AggregateId("2"), AggregateVersion.init, created, QueueBinding(queueId), Status.Ready, created, None, None, triggerDate, triggerDate.getMillis, sort, payload)
    val filters = Filters(List(Filter("data", Set(Some("value1"), Some("value2"), None))))
    readStore.retrieveByQueue(queueId, Status.Ready, PageRequest(pageSize), filters) returns PageResult(List(expectedTask2, expectedTask1), None, None)

    get("/queues/abc/ready?_data=value1&_data=value1&_data=value2&_data=", headers = validAuthHeader) {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedMultiParamFilteredQueue.json"))
    }
  }

  "return 200 with hal+json formatted queue response with pagination links" in {
    val expectedTask1 = TaskRecord(AggregateId("1"), AggregateVersion.init, created, QueueBinding(queueId), Status.Ready, created, None, None, triggerDate, triggerDate.getMillis, sort, payload)
    val expectedTask2 = TaskRecord(AggregateId("2"), AggregateVersion.init, created, QueueBinding(queueId), Status.Ready, created, None, None, triggerDate, triggerDate.getMillis, sort, payload)
    readStore.retrieveByQueue(queueId, Status.Ready, PageRequest(pageSize, Some(PageReference("3~1393336800000~0"))), Filters.empty) returns PageResult(List(expectedTask2, expectedTask1), Some(PageReference("2~1393336800000~0")), Some(PageReference("1~1393336800000~1")))

    get(s"/queues/abc/ready?page=3~1393336800000~0", headers = validAuthHeader) {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedQueueWithPaginationLinks.json"))
    }
  }

  "return 400 when queue status not recognised" in {
    get("/queues/abc/unrecognised", headers = validAuthHeader) {
      status must beEqualTo(400)
    }
  }

  "return 200 with hal+json formatted queue entry response" in {
    val expectedTask = TaskRecord(AggregateId("1"), AggregateVersion.init, created, QueueBinding(queueId), Status.Ready, created, None, None, triggerDate, triggerDate.getMillis, sort, payload)
    readStore.retrieveBy(expectedTask.id) returns Some(expectedTask)

    get(s"/queues/abc/ready/${expectedTask.id.value}", headers = validAuthHeader) {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedQueueEntry.json"))
    }
  }

  "return 404 when queue entry not found" in {
    readStore.retrieveBy(AggregateId("5")) returns None

    get("/queues/abc/ready/5", headers = validAuthHeader) {
      status must beEqualTo(404)
    }
  }

  "return 200 when posting task into started queue" in {
    val expectedTask = TaskRecord(AggregateId("3"), AggregateVersion.init, created, QueueBinding(queueId), Status.Ready, created, None, None, triggerDate, triggerDate.getMillis, sort, payload)
    readStore.retrieveBy(expectedTask.id) returns Some(expectedTask)

    post("/queues/abc/started", """{"id": "3", "assignee": "user1"}""", headers = validAuthHeader) {
      status must beEqualTo(200)
    }
  }

  "return 200 when requeuing tasks" in {
    val expectedTask = TaskRecord(AggregateId("3"), AggregateVersion.init, created, QueueBinding(queueId), Status.Started, created, None, None, triggerDate, triggerDate.getMillis, sort, payload)
    readStore.retrieveBy(expectedTask.id) returns Some(expectedTask)

    post("/queues/abc/ready", """{"id": "3"}""", headers = validAuthHeader) {
      status must beEqualTo(200)
    }
  }

  "return 200 when posting task into completed queue" in {
    val expectedTask = TaskRecord(AggregateId("4"), AggregateVersion.init, created, QueueBinding(queueId), Status.Started, created, None, None, triggerDate, triggerDate.getMillis, sort, payload)
    readStore.retrieveBy(expectedTask.id) returns Some(expectedTask)

    post("/queues/abc/completed", """{"id": "4"}""", headers = validAuthHeader) {
      status must beEqualTo(200)
    }
  }

  "return 401 when invalid auth credentials" in {
    get("/queues", headers = authHeader(validCredentials.username, "invalid")) {status must beEqualTo(401)}
  }

  def jsonFromFile(filePath: String) = {
    parse(textFromFile(filePath))
  }

  def textFromFile(filePath: String) = {
    fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString
  }

  def validAuthHeader = authHeader(validCredentials.username, validCredentials.password)
}
