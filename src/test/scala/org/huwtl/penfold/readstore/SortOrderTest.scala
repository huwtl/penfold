package org.huwtl.penfold.readstore

import org.specs2.mutable.Specification

class SortOrderTest extends Specification {

  "know how to parse from string" in {
    SortOrder.from("Asc") must beEqualTo(SortOrder.Asc)
    SortOrder.from("asC") must beEqualTo(SortOrder.Asc)
    SortOrder.from("Desc") must beEqualTo(SortOrder.Desc)
    SortOrder.from("unknown") must throwA[IllegalArgumentException]
  }
}
