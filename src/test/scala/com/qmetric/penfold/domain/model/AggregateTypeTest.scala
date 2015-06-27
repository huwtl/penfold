package com.qmetric.penfold.domain.model

import org.specs2.mutable.SpecificationWithJUnit

class AggregateTypeTest extends SpecificationWithJUnit {
  "parse from string" in {
    AggregateType.from("Task") must beSome(AggregateType.Task)
    AggregateType.from("unknown") must beNone
  }
}
