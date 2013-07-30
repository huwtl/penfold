package com.hlewis.eventfire.app.support

import org.scalatest.FunSpec
import io.Source._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import com.hlewis.eventfire.domain.{Payload, Job, Cron}
import org.scalatest.matchers.ShouldMatchers

class JobJsonConverterSpec extends FunSpec with ShouldMatchers {

  val converter = new JobJsonConverter

  describe("Job deserialization") {
    it("should deserialize json to job") {
      val json = fromInputStream(getClass.getClassLoader.getResourceAsStream("fixtures/job.json")).mkString

      val job = converter.jobFrom(json)

      job should equal(Job("12345678", "abc", Some(Cron("0", "0", "*", "*", "0", "*")), None, "waiting", Payload(Map("stuff" -> "something", "nested" -> Map("inner" -> true)))))
    }
  }

  describe("Job payload deserialization") {
    it("should deserialize json to payload") {
      val json = fromInputStream(getClass.getClassLoader.getResourceAsStream("fixtures/payload.json")).mkString

      val payload = converter.jobPayloadFrom(json)

      payload should equal(Payload(Map("stuff" -> "something", "nested" -> Map("inner" -> true))))
    }
  }

  describe("Job payload serialization") {
    it("should serialize payload to json") {
      val payload = Payload(Map("stuff" -> "something", "nested" -> Map("inner" -> true)))
      val expectedJson = pretty(parse(fromInputStream(getClass.getClassLoader.getResourceAsStream("fixtures/payload.json")).mkString))

      val json = converter.jsonFrom(payload)

      json should equal(expectedJson)
    }
  }
}