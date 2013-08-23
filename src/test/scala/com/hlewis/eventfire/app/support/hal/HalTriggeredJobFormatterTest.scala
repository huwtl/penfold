package com.hlewis.eventfire.app.support.hal

import java.net.URI
import scala.io.Source._
import com.hlewis.eventfire.domain.{Status, Payload, Job}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.specs2.mutable.Specification

class HalTriggeredJobFormatterTest extends Specification {
  val jobFormatter = new HalTriggeredJobFeedFormatter(new URI("http://host/triggered"), new URI("http://host/jobs"), new URI("http://host/started"))

  "format triggered job feed as hal+json" in {
    val job1 = Job("1", "", None, None, Status.Waiting, Payload(Map()))
    val job2 = Job("2", "", None, None, Status.Waiting, Payload(Map()))

    val hal = jobFormatter.halFrom(List(job2, job1))

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedTriggeredJobFeed.json"))
  }

  "format triggered job feed entry as hal+json" in {
    val job1 = Job("1", "", None, None, Status.Waiting, Payload(Map()))

    val hal = jobFormatter.halFrom(job1)

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedTriggeredJobFeedEntry.json"))
  }

  def jsonFromFile(filePath: String) = {
    parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString)
  }
}
