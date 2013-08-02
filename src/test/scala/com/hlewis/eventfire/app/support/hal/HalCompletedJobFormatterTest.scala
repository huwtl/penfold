package com.hlewis.eventfire.app.support.hal

import org.scalatest.FunSpec
import java.net.URI
import scala.io.Source._
import com.hlewis.eventfire.domain.Payload
import com.hlewis.eventfire.domain.Job
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.scalatest.matchers.ShouldMatchers

class HalCompletedJobFormatterTest extends FunSpec with ShouldMatchers {
  val jobFormatter = new HalCompletedJobFormatter(new URI("http://host/completed"))

  describe("HAL completed job feed formatter") {
    it("should format completed job feed entry as hal+json") {
      val job1 = Job("1", "", None, None, "", Payload(Map()))

      val hal = jobFormatter.halFrom(job1)

      parse(hal) should equal(jsonFromFile("fixtures/hal/halFormattedCompletedJobFeedEntry.json"))
    }

    def jsonFromFile(filePath: String) = {
      parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString)
    }
  }
}
