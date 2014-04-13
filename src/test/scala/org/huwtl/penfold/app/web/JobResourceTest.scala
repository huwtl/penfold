package org.huwtl.penfold.app.web

import org.huwtl.penfold.app.support.hal.HalJobFormatter
import java.net.URI
import org.json4s.jackson.JsonMethods._
import scala.io.Source._
import org.scalatra.test.specs2.MutableScalatraSpec
import org.joda.time.DateTime
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.readstore._
import org.huwtl.penfold.command.{CreateJob, CommandDispatcher}
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.readstore.PageRequest
import org.huwtl.penfold.readstore.Filter
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.readstore.JobRecord
import scala.Some
import org.huwtl.penfold.readstore.PageResult
import org.huwtl.penfold.app.AuthenticationCredentials

class JobResourceTest extends MutableScalatraSpec with Mockito with WebAuthSpecification {
  sequential

  val created = new DateTime(2014, 2, 14, 12, 0, 0, 0)

  val triggerDate = new DateTime(2014, 7, 10, 13, 5, 1, 0)

  val queueId = QueueId("abc")

  val binding = Binding(List(BoundQueue(queueId)))

  val pageSize = 5

  val validCredentials = AuthenticationCredentials("user", "secret")

  val  readStore = mock[ReadStore]

  val commandDispatcher = mock[CommandDispatcher]

  addServlet(new JobResource(readStore, commandDispatcher, new ObjectSerializer, new HalJobFormatter(new URI("http://host/jobs"), new URI("http://host/queues")), Some(validCredentials)), "/jobs/*")

  "return 200 with hal+json formatted job response" in {
    val expectedJob = JobRecord(AggregateId("1"), created, binding, Status.Waiting, triggerDate,triggerDate.getMillis , Payload(Map("data" -> "value", "inner" -> Map("bool" -> true))))
    readStore.retrieveBy(expectedJob.id) returns Some(expectedJob)

    get("/jobs/1", headers = validAuthHeader) {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedWaitingJob.json"))
    }
  }

  "return 200 with hal+json formatted filtered jobs response" in {
    val expectedJob = JobRecord(AggregateId("1"), created, binding, Status.Waiting, triggerDate, triggerDate.getMillis, Payload(Map("data" -> "value", "inner" -> Map("bool" -> true))))
    val filters = Filters(List(Filter("data", "value")))
    readStore.retrieveBy(filters, PageRequest(pageSize)) returns PageResult(List(expectedJob), previousExists = false, nextExists = false)

    get("/jobs?_data=value", headers = validAuthHeader) {
      status must beEqualTo(200)
      parse(body) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredJobs.json"))
    }
  }

  "return 404 when job does not exist" in {
    get("/jobs/notExists", headers = validAuthHeader) {
      status must beEqualTo(404)
    }
  }

  "return 201 when posting new job" in {
    val expectedJob = JobRecord(AggregateId("2"), created, binding, Status.Waiting, triggerDate, triggerDate.getMillis, Payload(Map("stuff" -> "something", "nested" -> Map("inner" -> true))))
    commandDispatcher.dispatch(CreateJob(binding, expectedJob.payload)) returns expectedJob.id
    readStore.retrieveBy(expectedJob.id) returns Some(expectedJob)

    post("/jobs", textFromFile("fixtures/web/job.json"), headers = validAuthHeader) {
      status must beEqualTo(201)
    }
  }

  "return 400 when posting invalid formatted json" in {
    post("/jobs", "{", headers = validAuthHeader) {
      status must beEqualTo(400)
    }
  }

  "return 400 when posting unexpected job json" in {
    post("/jobs", "{}", headers = validAuthHeader) {
      status must beEqualTo(400)
    }
  }

  "return 401 when invalid auth credentials" in {
    val url = "/jobs"
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
