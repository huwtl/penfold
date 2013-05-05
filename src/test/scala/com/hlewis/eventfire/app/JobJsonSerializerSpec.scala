package com.hlewis.eventfire.app

import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.{GivenWhenThen, FunSpec}
import net.liftweb.json._
import io.Source._
import com.hlewis.eventfire.domain.{Body, Cron, Header, Job}

class JobJsonSerializerSpec extends ScalatraSuite with FunSpec with GivenWhenThen {

  describe("Job JSON parsing") {
    it("should convert json to job") {
      given("json received for job")
      implicit val formats = Serialization.formats(NoTypeHints) + new JobJsonSerializer + new CronJsonSerializer
      val json = parse(fromInputStream(getClass.getClassLoader.getResourceAsStream("fixtures/job.json")).mkString)

      when("json is parsed")
      val job = json.extract[Job]

      then("expected job is created")
      job should equal(Job(Header("12345678", "abc", Cron("0", "0", "*", "*", "0"), Map("amqpExchange" -> "aaa", "amqpRoutingKey" -> "bbb")), Body(Map("stuff" -> "something", "boolean" -> true))))
    }
  }
}