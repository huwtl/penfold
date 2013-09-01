package org.huwtl.penfold.app.support.hal

import java.net.URI
import scala.io.Source._
import org.huwtl.penfold.domain._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.Id
import org.huwtl.penfold.domain.Payload
import org.huwtl.penfold.domain.Job

class HalCompletedJobFormatterTest extends Specification {
  val jobFormatter = new HalCompletedJobFormatter(new URI("http://host/completed"), new URI("http://host/jobs"))

  "format completed job feed entry as hal+json" in {
    val job1 = Job(Id("1"), JobType(""), None, None, Status.Waiting, Payload(Map()))

    val hal = jobFormatter.halFrom(job1)

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedCompletedJobFeedEntry.json"))
  }

  "format completed job feed as hal+json" in {
    val job1 = Job(Id("1"), JobType(""), None, None, Status.Waiting, Payload(Map()))
    val job2 = Job(Id("2"), JobType(""), None, None, Status.Waiting, Payload(Map()))

    val hal = jobFormatter.halFrom(List(job2, job1))

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedCompletedJobFeed.json"))
  }

  def jsonFromFile(filePath: String) = {
    parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString)
  }
}
