package org.huwtl.penfold.app.support.hal

import java.net.URI
import scala.io.Source._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.model.{Payload, QueueName, Status, Id}
import org.huwtl.penfold.query.JobRecord
import org.joda.time.DateTime

class HalStartedJobFormatterTest extends Specification {
  val jobFormatter = new HalStartedJobFormatter(new URI("http://host/started"), new URI("http://host/jobs"), new URI("http://host/completed"))

  "format started job feed entry as hal+json" in {
    val job = JobRecord(Id("1"), DateTime.now, QueueName(""), Status.Started, DateTime.now, Payload(Map()))

    val hal = jobFormatter.halFrom(job)

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedStartedJobFeedEntry.json"))
  }

  "format started job feed as hal+json" in {
    val job1 = JobRecord(Id("1"), DateTime.now, QueueName(""), Status.Started, DateTime.now, Payload(Map()))
    val job2 = JobRecord(Id("2"), DateTime.now, QueueName(""), Status.Started, DateTime.now, Payload(Map()))

    val hal = jobFormatter.halFrom(List(job2, job1))

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedStartedJobFeed.json"))
  }

  def jsonFromFile(filePath: String) = {
    parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString)
  }
}
