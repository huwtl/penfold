package org.huwtl.penfold.app.web

import org.huwtl.penfold.usecases.{RetrieveJobById, CreateJob}
import org.huwtl.penfold.app.support.JobJsonConverter
import org.huwtl.penfold.app.support.hal.HalJobFormatter
import java.net.URI
import org.json4s.jackson.JsonMethods._
import scala.io.Source._
import org.scalatra.test.specs2.MutableScalatraSpec
import org.huwtl.penfold.domain._
import org.joda.time.DateTime
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.Payload
import scala.Some
import org.huwtl.penfold.domain.Job
import org.huwtl.penfold.domain.Cron

class JobsResourceTest extends MutableScalatraSpec with Mockito {
  val retrieveJobById = mock[RetrieveJobById]

  val createJob = mock[CreateJob]

  addServlet(new JobsResource(retrieveJobById, createJob, new JobJsonConverter, new HalJobFormatter(new URI("http://host/jobs"), new URI("http://host/feed/triggered"))), "/jobs/*")

  "return 200 with hal+json formatted job response" in {
    val expectedJob = Job(Id("1234"), JobType("testType"), None, Some(new DateTime(2014, 7, 10, 13, 5, 1)), Status.Waiting, Payload(Map("data" -> "value", "inner" -> Map("bool" -> true))))
    retrieveJobById.retrieve(Id("1234")) returns Some(expectedJob)

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
    val expectedJob = Job(Id("12345678"), JobType("abc"), Some(Cron("0 0 * * 0 * *")), None, Status.Waiting, Payload(Map("stuff" -> "something", "nested" -> Map("inner" -> true))))
    createJob.create(expectedJob) returns expectedJob

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
