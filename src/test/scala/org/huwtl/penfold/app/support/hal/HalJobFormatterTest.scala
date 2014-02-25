package org.huwtl.penfold.app.support.hal

import java.net.URI
import scala.io.Source._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.joda.time.DateTime
import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.model.{Payload, Status, QueueName, Id}
import org.huwtl.penfold.query.JobRecord

class HalJobFormatterTest extends Specification {

  val id = Id("1")

  val created = new DateTime(2014, 2, 14, 12, 0, 0, 0)

  val triggerDate = new DateTime(2014, 7, 10, 13, 5, 1, 0)

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

  private def hal(status: Status) = {
    parse(jobFormatter.halFrom(JobRecord(id, created, queueName, status, triggerDate, payload)))
  }

  private def jsonFromFile(filePath: String) = {
    parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString)
  }
}
