package com.hlewis.eventfire.app.support.hal

import org.scalatest.FunSpec
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory
import java.net.URI
import scala.io.Source._
import com.hlewis.eventfire.domain.Payload
import scala.Some
import com.hlewis.eventfire.domain.Job
import com.hlewis.eventfire.domain.Cron
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.joda.time.DateTime
import org.scalatest.matchers.ShouldMatchers

class HalJobFormatterTest extends FunSpec with ShouldMatchers {
  val jobFormatter = new HalJobFormatter(new DefaultRepresentationFactory, new URI("http://host/jobs"), new URI("http://host/triggered"))

  describe("HAL job formatter") {
    it("should format cron job as hal+json") {
      val job = Job("1234", "testType", Some(Cron("01", "05", "13", "10", "07", "*", "2014")), None, "waiting", Payload(Map("data" -> "value", "inner" -> Map("bool" -> true))))

      val hal = jobFormatter.halFrom(job)

      parse(hal) should equal(jsonFromFile("fixtures/hal/halFormattedJob.json"))
    }

    it("should format job without cron as hal+json") {
      val job = Job("1234", "testType", None, Some(new DateTime(2014, 7, 10, 13, 5, 1)), "waiting", Payload(Map("data" -> "value", "inner" -> Map("bool" -> true))))

      val hal = jobFormatter.halFrom(job)

      parse(hal) should equal(jsonFromFile("fixtures/hal/halFormattedJobWithoutCron.json"))
    }

    def jsonFromFile(filePath: String) = {
      parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString)
    }
  }
}
