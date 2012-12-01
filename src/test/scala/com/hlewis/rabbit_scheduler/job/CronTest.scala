package com.hlewis.rabbit_scheduler.job

import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.FunSpec
import org.joda.time.DateTime._
import org.scala_tools.time.Imports._
import com.hlewis.support.GivenWhenThenLabelling

class CronTest extends ScalatraSuite with FunSpec with GivenWhenThenLabelling {
  describe("Cron definition") {
    it("should correctly calculate next execution date") {
      given
      val cron = Cron("59", "23", "*", "*", "*", String.valueOf(now.getYear))

      when
      val nextTriggerDateTime = cron.nextExecutionDate

      then
      nextTriggerDateTime should equal(now.hour(23).minute(59).second(0).withMillisOfSecond(0))
    }

    it("should return string representation") {
      given
      val cron = Cron("59", "23", "*", "*", "*", String.valueOf(now.getYear))

      when
      val string = cron.toString

      then
      string should equal("59 23 * * * " + String.valueOf(now.getYear))
    }

    it("should default missing year field to every year") {
      when
      val cronWithoutYearField = Cron("*", "*", "*", "*", "*")

      then
      cronWithoutYearField.year should equal("*")
    }
  }
}
