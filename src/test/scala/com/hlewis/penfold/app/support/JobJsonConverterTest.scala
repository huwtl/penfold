package com.hlewis.penfold.app.support

import io.Source._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import com.hlewis.penfold.domain.{Status, Payload, Job, Cron}
import org.specs2.mutable.Specification

class JobJsonConverterTest extends Specification {
  val converter = new JobJsonConverter

  "deserialise json to job" in {
    val json = fromInputStream(getClass.getClassLoader.getResourceAsStream("fixtures/job.json")).mkString

    val job = converter.jobFrom(json)

    job must beEqualTo(Job("12345678", "abc", Some(Cron("0 0 * * 0 * *")), None, Status.Waiting, Payload(Map("stuff" -> "something", "nested" -> Map("inner" -> true)))))
  }

  "deserialise json to payload" in {
    val json = fromInputStream(getClass.getClassLoader.getResourceAsStream("fixtures/payload.json")).mkString

    val payload = converter.jobPayloadFrom(json)

    payload must beEqualTo(Payload(Map("stuff" -> "something", "nested" -> Map("inner" -> true))))
  }

  "serialise payload to json" in {
    val payload = Payload(Map("stuff" -> "something", "nested" -> Map("inner" -> true)))
    val expectedJson = pretty(parse(fromInputStream(getClass.getClassLoader.getResourceAsStream("fixtures/payload.json")).mkString))

    val json = converter.jsonFrom(payload)

    json must beEqualTo(expectedJson)
  }
}