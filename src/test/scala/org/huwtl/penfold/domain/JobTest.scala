package org.huwtl.penfold.domain

import org.joda.time.DateTime
import org.specs2.mutable.Specification

class JobTest extends Specification {
  val triggerDate = new DateTime(2013, 7, 28, 12, 30, 0)

  "construct new job with trigger date calculated from cron" in {
    Job("", "", Some(Cron("0 30 12 28 07 * 2013")), None, Status.Waiting, Payload(Map())).nextTriggerDate must beEqualTo(triggerDate)
  }

  "construct new job with explicit trigger date" in {
    Job("", "", None, Some(triggerDate), Status.Waiting, Payload(Map())).nextTriggerDate must beEqualTo(triggerDate)
    Job("", "", Some(Cron("1 30 12 28 07 * 2013")), Some(triggerDate), Status.Waiting, Payload(Map())).nextTriggerDate must beEqualTo(triggerDate)
  }

  "construct new job with default trigger date" in {
    Job("", "", None, None, Status.Waiting, Payload(Map())).nextTriggerDate must not beNull
  }
}
