package com.qmetric.penfold.app.web

import com.qmetric.penfold.app.support.hal.HalTaskFormatter
import java.net.URI
import org.json4s.jackson.JsonMethods._
import scala.io.Source._
import org.scalatra.test.specs2.MutableScalatraSpec
import org.specs2.mock.Mockito
import com.qmetric.penfold.readstore._
import com.qmetric.penfold.command._
import com.qmetric.penfold.app.support.json.ObjectSerializer
import com.qmetric.penfold.readstore.Filter
import com.qmetric.penfold.readstore.PageResult
import com.qmetric.penfold.support.TestModel
import com.qmetric.penfold.domain.model.Status.Waiting
import com.qmetric.penfold.domain.model.AggregateId
import scala.Some
import com.qmetric.penfold.app.AuthenticationCredentials
import com.qmetric.penfold.readstore.PageRequest

class TaskResourceTest extends MutableScalatraSpec with Mockito with WebAuthSpecification {
  sequential

  val expectedTask = TestModel.readModels.task.copy(status = Waiting)

  val pageSize = 5

  val validCredentials = AuthenticationCredentials("user", "secret")

  val readStore = mock[ReadStore]

  val commandDispatcher = mock[CommandDispatcher]

  addServlet(new TaskResource(readStore, commandDispatcher, new TaskCommandParser(new ObjectSerializer), new HalTaskFormatter(new URI("http://host/tasks"), new URI("http://host/queues")), pageSize, Some(validCredentials)), "/tasks/*")

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
    commandDispatcher.dispatch(TestModel.commands.createTask) returns expectedTask.id
    readStore.retrieveBy(expectedTask.id) returns Some(expectedTask)

    post("/tasks", textFromFile("fixtures/web/create_task.json"), headers = validAuthHeader + commandTypeHeader("CreateTask")) {
      status must beEqualTo(201)
    }
  }

  "return 201 when posting new task scheduled in the future" in {
    commandDispatcher.dispatch(TestModel.commands.createFutureTask) returns expectedTask.id
    readStore.retrieveBy(expectedTask.id) returns Some(expectedTask)

    post("/tasks", textFromFile("fixtures/web/create_future_task.json"), headers = validAuthHeader + commandTypeHeader("CreateFutureTask")) {
      status must beEqualTo(201)
    }
  }

  "return 200 when unassigning task" in {
    commandDispatcher.dispatch(TestModel.commands.unassignTask) returns expectedTask.id
    readStore.retrieveBy(expectedTask.id) returns Some(expectedTask)

    post("/tasks/1/1", textFromFile("fixtures/web/unassign_task.json"), headers = validAuthHeader + commandTypeHeader("UnassignTask")) {
      status must beEqualTo(200)
    }
  }

  "return 200 when updating payload" in {
    commandDispatcher.dispatch(TestModel.commands.updateTaskPayload) returns expectedTask.id
    readStore.retrieveBy(expectedTask.id) returns Some(expectedTask)

    post("/tasks/1/1", textFromFile("fixtures/web/update_payload.json"), headers = validAuthHeader + commandTypeHeader("UpdateTaskPayload")) {
      status must beEqualTo(200)
    }
  }

  "return 200 when starting task" in {
    commandDispatcher.dispatch(TestModel.commands.startTask) returns expectedTask.id
    readStore.retrieveBy(expectedTask.id) returns Some(expectedTask)

    post("/tasks/1/1", textFromFile("fixtures/web/start_task.json"), headers = validAuthHeader + commandTypeHeader("StartTask")) {
      status must beEqualTo(200)
    }
  }

  "return 200 when requeuing task" in {
    commandDispatcher.dispatch(TestModel.commands.requeueTask) returns expectedTask.id
    readStore.retrieveBy(expectedTask.id) returns Some(expectedTask)

    post("/tasks/1/1", textFromFile("fixtures/web/requeue_task.json"), headers = validAuthHeader + commandTypeHeader("RequeueTask")) {
      status must beEqualTo(200)
    }
  }

  "return 200 when rescheduling task" in {
    commandDispatcher.dispatch(TestModel.commands.rescheduleTask) returns expectedTask.id
    readStore.retrieveBy(expectedTask.id) returns Some(expectedTask)

    post("/tasks/1/1", textFromFile("fixtures/web/reschedule_task.json"), headers = validAuthHeader + commandTypeHeader("RescheduleTask")) {
      status must beEqualTo(200)
    }
  }

  "return 200 when closing task" in {
    commandDispatcher.dispatch(TestModel.commands.closeTask) returns expectedTask.id
    readStore.retrieveBy(expectedTask.id) returns Some(expectedTask)

    post("/tasks/1/1", textFromFile("fixtures/web/close_task.json"), headers = validAuthHeader + commandTypeHeader("CloseTask")) {
      status must beEqualTo(200)
    }
  }

  "return 400 when missing content-type header from task creation request" in {
    commandDispatcher.dispatch(TestModel.commands.createTask) returns expectedTask.id
    readStore.retrieveBy(expectedTask.id) returns Some(expectedTask)

    post("/tasks", textFromFile("fixtures/web/create_task.json"), headers = validAuthHeader) {
      status must beEqualTo(400)
    }
  }

  "return 400 when missing content-type header from task update request" in {
    commandDispatcher.dispatch(TestModel.commands.startTask) returns expectedTask.id
    readStore.retrieveBy(expectedTask.id) returns Some(expectedTask)

    post("/tasks/1/1", textFromFile("fixtures/web/start_task.json"), headers = validAuthHeader) {
      status must beEqualTo(400)
    }
  }

  "return 400 when posting invalid formatted json" in {
    post("/tasks", "{", headers = validAuthHeader + commandTypeHeader("CreateTask")) {
      status must beEqualTo(400)
    }
  }

  "return 400 when posting unexpected task json" in {
    post("/tasks", "{}", headers = validAuthHeader + commandTypeHeader("CreateTask")) {
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

  def commandTypeHeader(commandType: String) = "Content-Type" -> s"application/json;domain-command=$commandType"

  def jsonFromFile(filePath: String) = {
    parse(textFromFile(filePath))
  }

  def textFromFile(filePath: String) = {
    fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString
  }

  def validAuthHeader = authHeader(validCredentials.username, validCredentials.password)
}
