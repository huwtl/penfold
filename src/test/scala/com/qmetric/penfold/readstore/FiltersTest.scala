package com.qmetric.penfold.readstore

import org.specs2.mutable.Specification

class FiltersTest extends Specification {
  "filters take multi params" in {
    val multiValues: Set[String] = Set("1", "2")
    val filters = Filters(List(In("a", multiValues)))
    filters.get("a").get.asInstanceOf[In].values must beEqualTo(Set("1", "2"))
  }

  "retrieve filter by key" in {
    val filters = Filters(List(Equals("a", "1"), Equals("b", "2")))
    filters.get("a") must beEqualTo(Some(Equals("a", "1")))
    filters.get("b") must beEqualTo(Some(Equals("b", "2")))
    filters.get("c") must beNone
  }
}
