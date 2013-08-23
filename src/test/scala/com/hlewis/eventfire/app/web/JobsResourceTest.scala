package com.hlewis.eventfire.app.web

import com.hlewis.eventfire.usecases.{RetrieveJobById, CreateJob}
import com.hlewis.eventfire.app.support.JobJsonConverter
import com.hlewis.eventfire.app.support.hal.HalJobFormatter
import com.hlewis.eventfire.app.store.InMemoryJobStore
import java.net.URI
import org.json4s.jackson.JsonMethods._
import scala.io.Source._
import org.scalatra.test.specs2.MutableScalatraSpec
import com.hlewis.eventfire.domain.{Payload, Status, Job}
import org.joda.time.DateTime
import org.specs2.specification.BeforeExample

class JobsResourceTest extends MutableScalatraSpec with BeforeExample {
  val store = new InMemoryJobStore

  addServlet(new JobsResource(new RetrieveJobById(store), new CreateJob(store), new JobJsonConverter, new HalJobFormatter(new URI("http://host/jobs"), new URI("http://host/feed/triggered"))), "/jobs/*")

  def before = {
    store.add(Job("1234", "testType", None, Some(new DateTime(2014, 7, 10, 13, 5, 1)), Status.Waiting, Payload(Map("data" -> "value", "inner" -> Map("bool" -> true)))))
  }

  "return 200 with hal+json formatted job response" in {
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
