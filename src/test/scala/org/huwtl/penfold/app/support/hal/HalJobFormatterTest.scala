package org.huwtl.penfold.app.support.hal

import java.net.URI
import scala.io.Source._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.joda.time.DateTime
import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.model.{Payload, Status, JobType, Id}
import org.huwtl.penfold.query.JobRecord

class HalJobFormatterTest extends Specification {
  val jobFormatter = new HalJobFormatter(new URI("http://host/jobs"), new URI("http://host/feed/triggered"))

  "format job as hal+json" in {
    val job = JobRecord(Id("1234"), new DateTime(2014, 2, 14, 12, 0, 0, 0), JobType("testType"), Status.Waiting, new DateTime(2014, 7, 10, 13, 5, 1, 0), Payload(Map("data" -> "value", "inner" -> Map("bool" -> true))))

    val hal = jobFormatter.halFrom(job)

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedJob.json"))
  }

  def jsonFromFile(filePath: String) = {
    parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString)
  }
}
