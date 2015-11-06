package com.qmetric.penfold.readstore

import org.specs2.mutable.SpecificationWithJUnit

class FiltersTest extends SpecificationWithJUnit {
  "filters take multi params" in {
    val multiValues: Set[String] = Set("1", "2")
    val filters = Filters(List(IN("a", multiValues)))
    filters.get("a").get.asInstanceOf[IN].values must beEqualTo(Set("1", "2"))
  }

  "retrieve filter by key" in {
    val filters = Filters(List(EQ("a", "1"), EQ("b", "2")))
    filters.get("a") must beEqualTo(Some(EQ("a", "1")))
    filters.get("b") must beEqualTo(Some(EQ("b", "2")))
    filters.get("c") must beNone
  }
}
