package com.qmetric.penfold.app.web

import com.qmetric.penfold.app.support.hal.HalTaskFormatter
import java.net.URI
import org.json4s.jackson.JsonMethods._
import scala.io.Source._
import org.scalatra.test.specs2.MutableScalatraSpec
import org.joda.time.DateTime
import org.specs2.mock.Mockito
import com.qmetric.penfold.domain.model._
import com.qmetric.penfold.readstore._
import com.qmetric.penfold.command.{UpdateTaskPayload, CreateTask, CommandDispatcher}
import com.qmetric.penfold.app.support.json.ObjectSerializer
import com.qmetric.penfold.domain.model.Payload
import com.qmetric.penfold.readstore.PageRequest
import com.qmetric.penfold.readstore.Filter
import com.qmetric.penfold.domain.model.QueueId
import com.qmetric.penfold.domain.model.AggregateId
import com.qmetric.penfold.readstore.TaskRecord
import scala.Some
import com.qmetric.penfold.readstore.PageResult
import com.qmetric.penfold.app.AuthenticationCredentials
import com.qmetric.penfold.domain.model.patch.{Patch, Value, Add}
import com.qmetric.penfold.support.TestModel
import com.qmetric.penfold.domain.model.Status.Waiting

class TaskResourceTest extends MutableScalatraSpec with Mockito with WebAuthSpecification {
  sequential

  val expectedTask = TestModel.task.copy(status = Waiting)

  val pageSize = 5

  val validCredentials = AuthenticationCredentials("user", "secret")

  val readStore = mock[ReadStore]

  val commandDispatcher = mock[CommandDispatcher]

  addServlet(new TaskResource(readStore, commandDispatcher, new ObjectSerializer, new HalTaskFormatter(new URI("http://host/tasks"), new URI("http://host/queues")), pageSize, Some(validCredentials)), "/tasks/*")

  "return 200 with hal+json formatted task response" in {
    readStore.retrieveBy(expectedTask.id) returns Some(expectedTask)

    get("/tasks/1", headers = validAuthHeader) {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedWaitingTask.json"))
    }
  }

  "return 200 with hal+json formatted filtered tasks response" in {
    val filters = Filters(List(Filter("data", Some("value"))))
    readStore.retrieveBy(filters, PageRequest(pageSize)) returns PageResult(List(expectedTask), None, None)

    get("/tasks?_data=value", headers = validAuthHeader) {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredTasks.json"))
    }
  }

  "return 404 when task does not exist" in {
    readStore.retrieveBy(AggregateId("notExists")) returns None
    get("/tasks/notExists", headers = validAuthHeader) {
      status must beEqualTo(404)
    }
  }

  "return 201 when posting new task" in {
    commandDispatcher.dispatch(CreateTask(QueueBinding(TestModel.queueId), expectedTask.payload, None)) returns expectedTask.id
    readStore.retrieveBy(expectedTask.id) returns Some(expectedTask)

    post("/tasks", textFromFile("fixtures/web/task.json"), headers = validAuthHeader) {
      status must beEqualTo(201)
    }
  }

  "return 200 when updating payload" in {
    commandDispatcher.dispatch(UpdateTaskPayload(expectedTask.id, expectedTask.version, Some("update_type_1"), Patch(List(Add("/a/b", Value("1")))), Some(100))) returns expectedTask.id
    readStore.retrieveBy(expectedTask.id) returns Some(expectedTask)

    put("/tasks/1/1/payload", textFromFile("fixtures/web/payload_update.json"), headers = validAuthHeader) {
      status must beEqualTo(200)
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
