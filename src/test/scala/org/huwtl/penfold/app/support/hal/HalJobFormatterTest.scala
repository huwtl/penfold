package org.huwtl.penfold.app.support.hal

import java.net.URI
import scala.io.Source._
import org.huwtl.penfold.domain._
import scala.Some
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.joda.time.DateTime
import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.Payload
import scala.Some
import org.huwtl.penfold.domain.Job
import org.huwtl.penfold.domain.Cron

class HalJobFormatterTest extends Specification {
  val jobFormatter = new HalJobFormatter(new URI("http://host/jobs"), new URI("http://host/feed/triggered"))

  "format cron job as hal+json" in {
    val job = Job(Id("1234"), JobType("testType"), Some(Cron("01 05 13 10 07 * 2014")), None, Status.Waiting, Payload(Map("data" -> "value", "inner" -> Map("bool" -> true))))

    val hal = jobFormatter.halFrom(job)

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedJobWithCron.json"))
  }

  "format job without cron as hal+json" in {
    val job = Job(Id("1234"), JobType("testType"), None, Some(new DateTime(2014, 7, 10, 13, 5, 1)), Status.Waiting, Payload(Map("data" -> "value", "inner" -> Map("bool" -> true))))

    val hal = jobFormatter.halFrom(job)

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedJob.json"))
  }

  def jsonFromFile(filePath: String) = {
    parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString)
  }
}
