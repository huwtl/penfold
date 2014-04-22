package org.huwtl.penfold.readstore

import org.specs2.mutable.Specification

class FiltersTest extends Specification {
  "filters take multi params" in {
    val multiValues: Set[Option[String]] = Set(Some("1"), Some("2"))
    val filters = Filters(List(Filter("a", multiValues)))
    filters.get("a").get.values must beEqualTo(Set(Some("1"), Some("2")))
  }

  "retrieve filter by key" in {
    val filters = Filters(List(Filter("a", Some("1")), Filter("b", Some("2"))))
    filters.get("a") must beEqualTo(Some(Filter("a", Some("1"))))
    filters.get("b") must beEqualTo(Some(Filter("b", Some("2"))))
    filters.get("c") must beNone
  }
}
