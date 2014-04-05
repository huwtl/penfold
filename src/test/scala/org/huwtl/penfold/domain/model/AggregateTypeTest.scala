package org.huwtl.penfold.domain.model

import org.specs2.mutable.Specification

class AggregateTypeTest extends Specification {
  "parse from string" in {
    AggregateType.from("Job") must beSome(AggregateType.Job)
    AggregateType.from("unknown") must beNone
  }
}
