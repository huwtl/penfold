package org.huwtl.penfold.domain

import org.joda.time.DateTime._
import org.scala_tools.time.Imports._
import org.specs2.mutable.Specification

class CronTest extends Specification {
  "calculate next execution date" in {
    val cron = Cron(s"0 59 23 * * * ${String.valueOf(now.getYear)}")

    val nextTriggerDateTime = cron.nextExecutionDate

    nextTriggerDateTime must beEqualTo(now.hour(23).minute(59).second(0).withMillisOfSecond(0))
  }

  "calculate next execution date for explicit date even if in past" in {
    val cron = Cron("0 59 23 1 12 * 2011")

    val nextTriggerDateTime = cron.nextExecutionDate

    nextTriggerDateTime must beEqualTo(new DateTime().withDate(2011, 12, 1).withTime(23, 59, 0, 0))
  }

  "return string representation" in {
    val cron = Cron(s"0 59 23 * * * ${String.valueOf(now.getYear)}")

    val string: String = cron.toString

    string must beEqualTo(s"0 59 23 * * * ${String.valueOf(now.getYear)}")
  }
}
