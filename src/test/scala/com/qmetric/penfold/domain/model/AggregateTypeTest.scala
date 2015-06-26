package com.qmetric.penfold.domain.model

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AggregateTypeTest extends Specification {
  "parse from string" in {
    AggregateType.from("Task") must beSome(AggregateType.Task)
    AggregateType.from("unknown") must beNone
  }
}
