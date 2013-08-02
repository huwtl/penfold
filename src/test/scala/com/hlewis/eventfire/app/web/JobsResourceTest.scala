package com.hlewis.eventfire.app.web

import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.{BeforeAndAfter, FunSpec}
import org.scalatest.mock.MockitoSugar
import com.hlewis.eventfire.usecases.{RetrieveJobById, CreateJob}
import com.hlewis.eventfire.app.support.JobJsonConverter
import com.hlewis.eventfire.app.support.hal.HalJobFormatter
import com.hlewis.eventfire.app.store.InMemoryJobStore
import java.net.URI
import org.joda.time.DateTime
import org.json4s.jackson.JsonMethods._
import scala.io.Source._
import com.hlewis.eventfire.domain.Payload
import scala.Some
import com.hlewis.eventfire.domain.Job

class JobsResourceTest extends ScalatraSuite with FunSpec with MockitoSugar with BeforeAndAfter {
  val store = new InMemoryJobStore

  addServlet(new JobsResource(new RetrieveJobById(store), new CreateJob(store), new JobJsonConverter, new HalJobFormatter(new URI("http://host/jobs"), new URI("http://host/feed/triggered"))), "/jobs/*")

  before {
    store.add(Job("1234", "testType", None, Some(new DateTime(2014, 7, 10, 13, 5, 1)), "waiting", Payload(Map("data" -> "value", "inner" -> Map("bool" -> true)))))
  }

  describe("Jobs resource") {
    it("should return 200 with hal+json formatted job response") {
      get("/jobs/1234") {
        status should be(200)
        parse(body) should equal(jsonFromFile("fixtures/hal/halFormattedJob.json"))
      }
    }

    it("should return 404 when job does not exist") {
      get("/jobs/notExists") {
        status should be(404)
      }
    }

    it("should return 201 when posting new job") {
      post("/jobs", textFromFile("fixtures/job.json")) {
        status should be(201)
      }
    }
  }

  def jsonFromFile(filePath: String) = {
    parse(textFromFile(filePath))
  }

  def textFromFile(filePath: String) = {
    fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString
  }
}
