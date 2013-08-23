package com.hlewis.eventfire.app.support

import org.json4s.jackson.JsonMethods._
import scala.io.Source._
import com.hlewis.eventfire.domain.StartJobRequest
import org.specs2.mutable.Specification

class StartJobRequestJsonConverterTest extends Specification {
  val converter = new StartJobRequestJsonConverter

  "deserialise job start request json" in {
    val expectedJson = pretty(parse(fromInputStream(getClass.getClassLoader.getResourceAsStream("fixtures/startJobRequest.json")).mkString))

    val startJobRequest = converter.from(expectedJson)

    startJobRequest must beEqualTo(StartJobRequest("12345678"))
  }
}
