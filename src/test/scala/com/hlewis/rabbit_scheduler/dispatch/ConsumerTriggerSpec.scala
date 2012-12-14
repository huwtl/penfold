package com.hlewis.rabbit_scheduler.dispatch

import org.scalatest.FunSpec
import org.scalatra.test.scalatest.ScalatraSuite
import com.hlewis.support.GivenWhenThenLabelling
import com.hlewis.rabbit_scheduler.domain.StartActor

class ConsumerTriggerSpec extends ScalatraSuite with FunSpec with GivenWhenThenLabelling {

  describe("Consumer trigger") {
    it("should trigger every 1 minute"){
      StartActor.start()

      Thread.sleep(10000)
    }
  }
}
