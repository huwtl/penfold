package com.qmetric.penfold.app.web

import com.qmetric.penfold.app.support.hal.{HalQueueFormatter, HalTaskFormatter}
import java.net.URI
import org.json4s.jackson.JsonMethods._
import scala.io.Source._
import org.scalatra.test.specs2.MutableScalatraSpec
import org.specs2.mock.Mockito
import com.qmetric.penfold.domain.model._
import com.qmetric.penfold.readstore._
import com.qmetric.penfold.command.CommandDispatcher
import com.qmetric.penfold.domain.model.AggregateId
import scala.Some
import com.qmetric.penfold.readstore.PageRequest
import com.qmetric.penfold.readstore.PageResult
import com.qmetric.penfold.app.AuthenticationCredentials
import com.qmetric.penfold.support.TestModel

class QueueResourceTest extends MutableScalatraSpec with Mockito with WebAuthSpecification {
  sequential

  val expectedTask1 = TestModel.readModels.task.copy(id = AggregateId("1"))

  val expectedTask2 = TestModel.readModels.task.copy(id = AggregateId("2"))

  val pageSize = 5

  val sortOrderMapping = SortOrderMapping(Map(Status.Waiting -> SortOrder.Desc, Status.Ready -> SortOrder.Desc, Status.Started -> SortOrder.Desc, Status.Closed -> SortOrder.Desc))

  val validCredentials = AuthenticationCredentials("user", "secret")

  val readStore = mock[ReadStore]

  val commandDispatcher = mock[CommandDispatcher]

  addServlet(new QueueResource(
    readStore,
    new HalQueueFormatter(new URI("http://host/queues"), new HalTaskFormatter(new URI("http://host/tasks"), new URI("http://host/queues"))),
    sortOrderMapping,
    pageSize,
    Some(validCredentials)), "/queues/*")

  "return 200 with hal+json formatted queue response" in {
    readStore.retrieveByQueue(TestModel.queueId, Status.Ready, PageRequest(pageSize), SortOrder.Desc, Filters.empty) returns PageResult(List(expectedTask2, expectedTask1), None, None)

    get("/queues/abc/ready", headers = validAuthHeader) {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedQueue.json"))
    }
  }

  "return 200 with hal+json formatted filtered queue response" in {
    val filters = Filters(List(Equals("data", "a value")))
    readStore.retrieveByQueue(TestModel.queueId, Status.Ready, PageRequest(pageSize), SortOrder.Desc, filters) returns PageResult(List(expectedTask2, expectedTask1), None, None)

    get("/queues/abc/ready?q=%5B%7B%22op%22%3A%22Equals%22%2C%22key%22%3A%22data%22%2C%22value%22%3A%22a%20value%22%2C%22dataType%22%3A%22string%22%7D%5D", headers = validAuthHeader) {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredQueue.json"))
    }
  }

  "return 200 with hal+json formatted filtered queue response with multparams" in {
    val filters = Filters(List(In("data", Set("value1", "value2", null))))
    readStore.retrieveByQueue(TestModel.queueId, Status.Ready, PageRequest(pageSize), SortOrder.Desc, filters) returns PageResult(List(expectedTask2, expectedTask1), None, None)

    get("/queues/abc/ready?q=%5B%7B%22op%22%3A%22In%22%2C%22key%22%3A%22data%22%2C%22values%22%3A%5Bnull%2C%22value1%22%2C%22value2%22%5D%2C%22dataType%22%3A%22string%22%7D%5D", headers = validAuthHeader) {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedMultiParamFilteredQueue.json"))
    }
  }

  "return 200 with hal+json formatted queue response with pagination links" in {
    readStore.retrieveByQueue(TestModel.queueId, Status.Ready, PageRequest(pageSize, Some(PageReference("3~1393336800000~0"))), SortOrder.Desc, Filters.empty) returns PageResult(List(expectedTask2, expectedTask1), Some(PageReference("2~1393336800000~0")), Some(PageReference("1~1393336800000~1")))

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
    readStore.retrieveBy(expectedTask1.id) returns Some(expectedTask1)

    get(s"/queues/abc/ready/${expectedTask1.id.value}", headers = validAuthHeader) {
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
