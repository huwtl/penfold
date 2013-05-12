package com.hlewis.eventfire.domain

import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.FunSpec
import org.joda.time.DateTime._
import org.scala_tools.time.Imports._

class CronSpec extends ScalatraSuite with FunSpec {
  describe("Cron definition") {
    it("should correctly calculate next execution date") {
      val cron = Cron("59", "23", "*", "*", "*", String.valueOf(now.getYear))

      val nextTriggerDateTime = cron.nextExecutionDate

      nextTriggerDateTime should equal(now.hour(23).minute(59).second(0).withMillisOfSecond(0))
    }

    it("should calculate next execution for explicit date even if in past") {
      val cron = Cron("59", "23", "1", "12", "*", "2011")

      val nextTriggerDateTime = cron.nextExecutionDate

      nextTriggerDateTime should equal(new DateTime().withDate(2011, 12, 1).withTime(23, 59, 0, 0))
    }

    it("should return string representation") {
      val cron = Cron("59", "23", "*", "*", "*", String.valueOf(now.getYear))

      val string = cron.toString

      string should equal("59 23 * * * " + String.valueOf(now.getYear))
    }

    it("should default missing year field to every year") {
      val cronWithoutYearField = Cron("*", "*", "*", "*", "*")

      cronWithoutYearField.year should equal("*")
    }
  }
}
