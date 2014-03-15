package org.huwtl.penfold.app.web

import org.huwtl.penfold.app.support.hal.HalJobFormatter
import java.net.URI
import org.json4s.jackson.JsonMethods._
import scala.io.Source._
import org.scalatra.test.specs2.MutableScalatraSpec
import org.joda.time.DateTime
import org.specs2.mock.Mockito
import scala.Some
import org.huwtl.penfold.domain.model.{QueueName, Status, Payload, AggregateId}
import org.huwtl.penfold.query._
import org.huwtl.penfold.command.CommandDispatcher
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.specs2.specification.Scope
import org.huwtl.penfold.domain.model.QueueName
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.query.PageRequest
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.query.JobRecord
import scala.Some

class JobResourceTest extends MutableScalatraSpec with Mockito {
  sequential

  val created = new DateTime(2014, 2, 14, 12, 0, 0, 0)

  val triggerDate = new DateTime(2014, 7, 10, 13, 5, 1, 0)

  val queueName = QueueName("abc")

  val queryRepository = mock[QueryRepository]

  val commandDispatcher = mock[CommandDispatcher]

  addServlet(new JobResource(queryRepository, commandDispatcher, new ObjectSerializer, new HalJobFormatter(new URI("http://host/jobs"), new URI("http://host/queues"))), "/jobs/*")

  "return 200 with hal+json formatted job response" in {
    val expectedJob = JobRecord(AggregateId("1"), created, queueName, Status.Waiting, triggerDate, Payload(Map("data" -> "value", "inner" -> Map("bool" -> true))))
    queryRepository.retrieveBy(expectedJob.id) returns Some(expectedJob)

    get("/jobs/1") {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedWaitingJob.json"))
    }
  }

  "return 200 with hal+json formatted filtered jobs response" in {
    val expectedJob = JobRecord(AggregateId("1"), created, queueName, Status.Waiting, triggerDate, Payload(Map("data" -> "value", "inner" -> Map("bool" -> true))))
    val filters = Filters(List(Filter("data", "value")))
    queryRepository.retrieveBy(filters, PageRequest(0, 10)) returns PageResult(0, List(expectedJob), previousExists = false, nextExists = false)

    get("/jobs?_data=value") {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredJobs.json"))
    }
  }

  "return 404 when job does not exist" in {
    get("/jobs/notExists") {
      status must beEqualTo(404)
    }
  }

  "return 201 when posting new job" in {
    val expectedJob = JobRecord(AggregateId("2"), created, queueName, Status.Waiting, triggerDate, Payload(Map("stuff" -> "something", "nested" -> Map("inner" -> true))))
    queryRepository.retrieveBy(expectedJob.id) returns Some(expectedJob)

    post("/jobs", textFromFile("fixtures/job.json")) {
      status must beEqualTo(201)
    }
  }

  def jsonFromFile(filePath: String) = {
    parse(textFromFile(filePath))
  }

  def textFromFile(filePath: String) = {
    fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString
  }
}
