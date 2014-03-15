package org.huwtl.penfold.app.support

import org.specs2.mutable.Specification

class ListCombinerTest extends Specification {

  "combine lists" in {
    combine(List(Nil)) must beEqualTo(List())
    combine(List(List("a"), Nil)) must beEqualTo(List())
    combine(List(Nil, List("a"))) must beEqualTo(List())
    combine(List(List("a"), List("b"))) must beEqualTo(List(List("a", "b")))
    combine(List(List("a"), List("b", "c"))) must beEqualTo(List(List("a", "b"), List("a", "c")))
    combine(List(List("a", "b"), List("c", "d"))) must beEqualTo(List(List("a", "c"), List("a", "d"), List("b", "c"), List("b", "d")))
  }

  private def combine(lists: List[List[String]]) = ListCombiner.combine[String](lists)
}
