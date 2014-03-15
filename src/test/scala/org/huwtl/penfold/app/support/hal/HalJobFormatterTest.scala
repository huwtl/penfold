package org.huwtl.penfold.app.support.hal

import java.net.URI
import scala.io.Source._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.joda.time.DateTime
import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.model.{Payload, Status, QueueName, AggregateId}
import org.huwtl.penfold.query.{Filter, Filters, PageResult, JobRecord}

class HalJobFormatterTest extends Specification {

  val id = AggregateId("1")

  val created = new DateTime(2014, 2, 14, 12, 0, 0, 0)

  val triggerDate = new DateTime(2014, 7, 10, 13, 5, 1, 0)

  val filters = Filters(List(Filter("data", "value")))

  val queueName = QueueName("abc")

  val payload = Payload(Map("data" -> "value", "inner" -> Map("bool" -> true)))

  val jobFormatter = new HalJobFormatter(new URI("http://host/jobs"), new URI("http://host/queues"))

  "format waiting job as hal+json" in {
    hal(Status.Waiting) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedWaitingJob.json"))
  }

  "format triggered job as hal+json" in {
    hal(Status.Triggered) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedTriggeredJob.json"))
  }

  "format started job as hal+json" in {
    hal(Status.Started) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedStartedJob.json"))
  }

  "format completed job as hal+json" in {
    hal(Status.Completed) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedCompletedJob.json"))
  }

  "format cancelled job as hal+json" in {
    hal(Status.Cancelled) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedCancelledJob.json"))
  }

  "format filtered jobs hal+json" in {
    halJobs(filters) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredJobs.json"))
  }

  "format filtered jobs hal+json with encoded filter value" in {
    val filters = Filters(List(Filter("data", "zzz%^&*ee$")))
    halJobs(filters) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredJobsWithEncodedFilterValue.json"))
  }

  "format filtered jobs hal+json with pagination links" in {
    halJobs(filters, 1, previousPage = true, nextPage = true) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredJobsWithPaginationLinks.json"))
  }

  "format job as hal+json with complex payload" in {
    val complexPayload = Payload(
      Map("data" -> "value", "inner" -> Map("bool" -> true, "inner2" -> List(Map("a" -> "1", "b" -> 1), Map("a" -> "2", "b" -> 2)))))
    val job = JobRecord(id, created, queueName, Status.Waiting, triggerDate, complexPayload)
    hal(job) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedJobWithComplexPayload.json"))
  }

  private def halJobs(filters: Filters, pageNumber: Int = 0, previousPage: Boolean = false, nextPage: Boolean = false) = {
    parse(jobFormatter.halFrom(
      PageResult(pageNumber, List(JobRecord(id, created, queueName, Status.Waiting, triggerDate, payload)), previousExists = previousPage, nextExists = nextPage), filters
    ))
  }

  private def hal(status: Status) = {
    parse(jobFormatter.halFrom(JobRecord(id, created, queueName, status, triggerDate, payload)))
  }

  private def hal(job: JobRecord) = {
    parse(jobFormatter.halFrom(job))
  }

  private def jsonFromFile(filePath: String) = {
    parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString)
  }
}
