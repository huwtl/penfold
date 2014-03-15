package org.huwtl.penfold.app.web

import org.huwtl.penfold.app.support.hal.{HalQueueFormatter, HalJobFormatter}
import java.net.URI
import org.json4s.jackson.JsonMethods._
import scala.io.Source._
import org.scalatra.test.specs2.MutableScalatraSpec
import org.joda.time.DateTime
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model.Status
import org.huwtl.penfold.query._
import org.huwtl.penfold.command.CommandDispatcher
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.model.QueueName
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.query.PageRequest
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.query.JobRecord
import scala.Some
import org.huwtl.penfold.query.PageResult

class QueueResourceTest extends MutableScalatraSpec with Mockito {
  sequential

  val queueName = QueueName("abc")

  val payload = Payload(Map())

  val created = new DateTime(2014, 2, 14, 12, 0, 0, 0)

  val triggerDate = new DateTime(2014, 2, 25, 14, 0, 0, 0)

  val pageSize = 10

  val queryRepository = mock[QueryRepository]

  val commandDispatcher = mock[CommandDispatcher]

  addServlet(new QueueResource(queryRepository, commandDispatcher, new ObjectSerializer, new HalQueueFormatter(new URI("http://host/queues"), new HalJobFormatter(new URI("http://host/jobs"), new URI("http://host/queues")))), "/queues/*")

  "return 200 with hal+json formatted queue response" in {
    val expectedJob1 = JobRecord(AggregateId("1"), created, queueName, Status.Triggered, triggerDate, payload)
    val expectedJob2 = JobRecord(AggregateId("2"), created, queueName, Status.Triggered, triggerDate, payload)
    queryRepository.retrieveBy(queueName, Status.Triggered, PageRequest(0, pageSize), Filters.empty) returns PageResult(0, List(expectedJob2, expectedJob1), previousExists = false, nextExists = false)

    get("/queues/abc/triggered") {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedQueue.json"))
    }
  }

  "return 200 with hal+json formatted filtered queue response" in {
    val expectedJob1 = JobRecord(AggregateId("1"), created, queueName, Status.Triggered, triggerDate, payload)
    val expectedJob2 = JobRecord(AggregateId("2"), created, queueName, Status.Triggered, triggerDate, payload)
    val filters = Filters(List(Filter("data", "value")))
    queryRepository.retrieveBy(queueName, Status.Triggered, PageRequest(0, pageSize), filters) returns PageResult(0, List(expectedJob2, expectedJob1), previousExists = false, nextExists = false)

    get("/queues/abc/triggered?_data=value") {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredQueue.json"))
    }
  }

  "return 200 with hal+json formatted queue response with pagination links" in {
    val expectedJob1 = JobRecord(AggregateId("1"), created, queueName, Status.Triggered, triggerDate, payload)
    val expectedJob2 = JobRecord(AggregateId("2"), created, queueName, Status.Triggered, triggerDate, payload)
    queryRepository.retrieveBy(queueName, Status.Triggered, PageRequest(1, pageSize), Filters.empty) returns PageResult(1, List(expectedJob2, expectedJob1), previousExists = true, nextExists = true)

    get("/queues/abc/triggered?page=1") {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedQueueWithPaginationLinks.json"))
    }
  }

  "return 404 when queue status not recognised" in {
    get("/queues/abc/notExists") {
      status must beEqualTo(404)
    }
  }

  "return 200 with hal+json formatted queue entry response" in {
    val expectedJob = JobRecord(AggregateId("1"), created, queueName, Status.Triggered, triggerDate, payload)
    queryRepository.retrieveBy(expectedJob.id) returns Some(expectedJob)

    get(s"/queues/abc/triggered/${expectedJob.id.value}") {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedQueueEntry.json"))
    }
  }

  "return 404 when queue entry not found" in {
    get("/queues/abc/triggered/5") {
      queryRepository.retrieveBy(AggregateId("5")) returns None
      status must beEqualTo(404)
    }
  }

  "return 200 when posting job into started queue" in {
    val expectedJob = JobRecord(AggregateId("3"), created, queueName, Status.Triggered, triggerDate, payload)
    queryRepository.retrieveBy(expectedJob.id) returns Some(expectedJob)

    post("/queues/abc/started", """{"id": "3"}""") {
      status must beEqualTo(200)
    }
  }

  "return 200 when posting job into completed queue" in {
    val expectedJob = JobRecord(AggregateId("4"), created, queueName, Status.Started, triggerDate, payload)
    queryRepository.retrieveBy(expectedJob.id) returns Some(expectedJob)

    post("/queues/abc/completed", """{"id": "4"}""") {
      status must beEqualTo(200)
    }
  }

  def jsonFromFile(filePath: String) = {
    parse(textFromFile(filePath))
  }

  def textFromFile(filePath: String) = {
    fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString
  }
}
