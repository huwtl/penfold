package org.huwtl.penfold.app.web

import org.huwtl.penfold.app.support.hal.HalJobFormatter
import java.net.URI
import org.json4s.jackson.JsonMethods._
import scala.io.Source._
import org.scalatra.test.specs2.MutableScalatraSpec
import org.joda.time.DateTime
import org.specs2.mock.Mockito
import scala.Some
import org.huwtl.penfold.domain.model.{QueueName, Status, Payload, Id}
import org.huwtl.penfold.query.{JobRecord, QueryRepository}
import org.huwtl.penfold.command.CommandDispatcher
import org.huwtl.penfold.app.support.json.ObjectSerializer

class JobsResourceTest extends MutableScalatraSpec with Mockito {
  sequential

  val queryRepository = mock[QueryRepository]

  val commandDispatcher = mock[CommandDispatcher]

  addServlet(new JobsResource(queryRepository, commandDispatcher, new ObjectSerializer, new HalJobFormatter(new URI("http://host/jobs"), new URI("http://host/feed/triggered"))), "/jobs/*")

  "return 200 with hal+json formatted job response" in {
    val expectedJob = JobRecord(Id("1234"), new DateTime(2014, 2, 14, 12, 0, 0, 0), QueueName("testType"), Status.Waiting, new DateTime(2014, 7, 10, 13, 5, 1, 0), Payload(Map("data" -> "value", "inner" -> Map("bool" -> true))))
    queryRepository.retrieveBy(expectedJob.id) returns Some(expectedJob)

    get("/jobs/1234") {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedJob.json"))
    }
  }

  "return 404 when job does not exist" in {
    get("/jobs/notExists") {
      status must beEqualTo(404)
    }
  }

  "return 201 when posting new job" in {
    val expectedJob = JobRecord(Id("12345678"), new DateTime(2014, 2, 14, 12, 0, 0, 0), QueueName("abc"), Status.Waiting, new DateTime(2014, 7, 10, 13, 5, 1, 0), Payload(Map("stuff" -> "something", "nested" -> Map("inner" -> true))))
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
