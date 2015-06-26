package com.qmetric.penfold.readstore

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SortOrderTest extends Specification {

  "know how to parse from string" in {
    SortOrder.from("Asc") must beEqualTo(SortOrder.Asc)
    SortOrder.from("asC") must beEqualTo(SortOrder.Asc)
    SortOrder.from("Desc") must beEqualTo(SortOrder.Desc)
    SortOrder.from("unknown") must throwA[IllegalArgumentException]
  }
}
