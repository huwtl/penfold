package org.huwtl.penfold.app.web

import org.huwtl.penfold.app.support.hal.HalTaskFormatter
import java.net.URI
import org.json4s.jackson.JsonMethods._
import scala.io.Source._
import org.scalatra.test.specs2.MutableScalatraSpec
import org.joda.time.DateTime
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.readstore._
import org.huwtl.penfold.command.{CreateTask, CommandDispatcher}
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.readstore.PageRequest
import org.huwtl.penfold.readstore.Filter
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.readstore.TaskRecord
import scala.Some
import org.huwtl.penfold.readstore.PageResult
import org.huwtl.penfold.app.AuthenticationCredentials

class TaskResourceTest extends MutableScalatraSpec with Mockito with WebAuthSpecification {
  sequential

  val created = new DateTime(2014, 2, 14, 12, 0, 0, 0)

  val triggerDate = new DateTime(2014, 2, 25, 14, 0, 0, 0)

  val queueId = QueueId("abc")

  val binding = QueueBinding(queueId)

  val pageSize = 5

  val validCredentials = AuthenticationCredentials("user", "secret")

  val  readStore = mock[ReadStore]

  val commandDispatcher = mock[CommandDispatcher]

  addServlet(new TaskResource(readStore, commandDispatcher, new ObjectSerializer, new HalTaskFormatter(new URI("http://host/tasks"), new URI("http://host/queues")), pageSize, Some(validCredentials)), "/tasks/*")

  "return 200 with hal+json formatted task response" in {
    val expectedTask = TaskRecord(AggregateId("1"), created, binding, Status.Waiting, created, triggerDate, triggerDate.getMillis, Payload(Map("data" -> "value", "inner" -> Map("bool" -> true))))
    readStore.retrieveBy(expectedTask.id) returns Some(expectedTask)

    get("/tasks/1", headers = validAuthHeader) {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedWaitingTask.json"))
    }
  }

  "return 200 with hal+json formatted filtered tasks response" in {
    val expectedTask = TaskRecord(AggregateId("1"), created, binding, Status.Waiting, created, triggerDate, triggerDate.getMillis, Payload(Map("data" -> "value", "inner" -> Map("bool" -> true))))
    val filters = Filters(List(Filter("data", Some("value"))))
    readStore.retrieveBy(filters, PageRequest(pageSize)) returns PageResult(List(expectedTask), None, None)

    get("/tasks?_data=value", headers = validAuthHeader) {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredTasks.json"))
    }
  }

  "return 404 when task does not exist" in {
    get("/tasks/notExists", headers = validAuthHeader) {
      status must beEqualTo(404)
    }
  }

  "return 201 when posting new task" in {
    val expectedTask = TaskRecord(AggregateId("2"), created, binding, Status.Waiting, created, triggerDate, triggerDate.getMillis, Payload(Map("stuff" -> "something", "nested" -> Map("inner" -> true))))
    commandDispatcher.dispatch(CreateTask(binding, expectedTask.payload)) returns expectedTask.id
    readStore.retrieveBy(expectedTask.id) returns Some(expectedTask)

    post("/tasks", textFromFile("fixtures/web/task.json"), headers = validAuthHeader) {
      status must beEqualTo(201)
    }
  }

  "return 400 when posting invalid formatted json" in {
    post("/tasks", "{", headers = validAuthHeader) {
      status must beEqualTo(400)
    }
  }

  "return 400 when posting unexpected task json" in {
    post("/tasks", "{}", headers = validAuthHeader) {
      status must beEqualTo(400)
    }
  }

  "return 401 when invalid auth credentials" in {
    val url = "/tasks"
    get(url) {status must beEqualTo(401)}
    get(url, headers = authHeader(validCredentials.username, null)) {status must beEqualTo(401)}
    get(url, headers = authHeader(validCredentials.username, "invalid")) {status must beEqualTo(401)}
    get(url, headers = authHeader("wrong", validCredentials.password)) {status must beEqualTo(401)}
    get(url, headers = authHeader(validCredentials.username, validCredentials.password + "2")) {status must beEqualTo(401)}
  }

  def jsonFromFile(filePath: String) = {
    parse(textFromFile(filePath))
  }

  def textFromFile(filePath: String) = {
    fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString
  }

  def validAuthHeader = authHeader(validCredentials.username, validCredentials.password)
}
