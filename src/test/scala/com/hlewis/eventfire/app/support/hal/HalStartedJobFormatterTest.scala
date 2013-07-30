package com.hlewis.eventfire.app.support.hal

import org.scalatest.FunSpec
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory
import java.net.URI
import scala.io.Source._
import com.hlewis.eventfire.domain.Payload
import com.hlewis.eventfire.domain.Job
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.scalatest.matchers.ShouldMatchers

class HalStartedJobFormatterTest extends FunSpec with ShouldMatchers {
  val jobFormatter = new HalStartedJobFormatter(new DefaultRepresentationFactory, new URI("http://host/started"), new URI("http://host/jobs"), new URI("http://host/completed"))

  describe("HAL started job feed formatter") {
    it("should format started job feed entry as hal+json") {
      val job1 = Job("1", "", None, None, "", Payload(Map()))

      val hal = jobFormatter.halFrom(job1)

      parse(hal) should equal(jsonFromFile("fixtures/hal/halFormattedStartedJobFeedEntry.json"))
    }

    def jsonFromFile(filePath: String) = {
      parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString)
    }
  }
}
