package com.hlewis.eventfire.app.support

import org.scalatest.FunSpec
import org.json4s.jackson.JsonMethods._
import scala.io.Source._
import com.hlewis.eventfire.domain.StartJobRequest
import org.scalatest.matchers.ShouldMatchers

class StartJobRequestJsonConverterTest extends FunSpec with ShouldMatchers {

  val converter = new StartJobRequestJsonConverter

  describe("Start job request deserialization") {
    it("should deserialize job start request json") {
      val expectedJson = pretty(parse(fromInputStream(getClass.getClassLoader.getResourceAsStream("fixtures/startJobRequest.json")).mkString))

      val startJobRequest = converter.from(expectedJson)

      startJobRequest should equal(StartJobRequest("12345678"))
    }
  }
}
