package org.huwtl.penfold.app.support.hal

import java.net.URI
import scala.io.Source._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.model.{Payload, Status, QueueName, Id}
import org.huwtl.penfold.query.JobRecord
import org.joda.time.DateTime

class HalCompletedJobFormatterTest extends Specification {
  val jobFormatter = new HalCompletedJobFormatter(new URI("http://host/completed"), new URI("http://host/jobs"))

  "format completed job feed entry as hal+json" in {
    val job1 = JobRecord(Id("1"), DateTime.now, QueueName(""), Status.Completed, DateTime.now, Payload(Map()))

    val hal = jobFormatter.halFrom(job1)

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedCompletedJobFeedEntry.json"))
  }

  "format completed job feed as hal+json" in {
    val job1 = JobRecord(Id("1"), DateTime.now, QueueName(""), Status.Completed, DateTime.now, Payload(Map()))
    val job2 = JobRecord(Id("2"), DateTime.now, QueueName(""), Status.Completed, DateTime.now, Payload(Map()))

    val hal = jobFormatter.halFrom(List(job2, job1))

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedCompletedJobFeed.json"))
  }

  def jsonFromFile(filePath: String) = {
    parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString)
  }
}
