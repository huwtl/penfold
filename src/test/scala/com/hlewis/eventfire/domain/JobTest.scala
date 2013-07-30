package com.hlewis.eventfire.domain

import org.scalatest.FunSpec
import org.joda.time.DateTime
import org.scalatest.matchers.ShouldMatchers

class JobTest extends FunSpec with ShouldMatchers {
  describe("Job") {
    it("should construct new job with trigger date calculated from cron") {
      Job("", "", Some(Cron("0", "30", "12", "28", "07", "*", "2013")), None, "", Payload(Map())).nextTriggerDate should equal(new DateTime(2013, 7, 28, 12, 30, 0))
    }

    it("should construct new job with explicit trigger date") {
      Job("", "", None, Some(new DateTime(2013, 7, 28, 12, 30, 0)), "", Payload(Map())).nextTriggerDate should equal(new DateTime(2013, 7, 28, 12, 30, 0))
    }

    it("should construct new job with default trigger date") {
      Job("", "", None, None, "", Payload(Map())).nextTriggerDate should not be null
    }
  }
}
