package com.qmetric.penfold.readstore

import org.specs2.mutable.SpecificationWithJUnit

class SortOrderTest extends SpecificationWithJUnit {

  "know how to parse from string" in {
    SortOrder.from("Asc") must beEqualTo(SortOrder.Asc)
    SortOrder.from("asC") must beEqualTo(SortOrder.Asc)
    SortOrder.from("Desc") must beEqualTo(SortOrder.Desc)
    SortOrder.from("unknown") must throwA[IllegalArgumentException]
  }
}
