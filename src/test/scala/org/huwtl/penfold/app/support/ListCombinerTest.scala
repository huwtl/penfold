package org.huwtl.penfold.app.support

import org.specs2.mutable.Specification

class ListCombinerTest extends Specification {

  "combine lists" in {
    combine(List(Nil)) must beEqualTo(Nil)
    combine(List(List("a"), Nil)) must beEqualTo(Nil)
    combine(List(Nil, List("a"))) must beEqualTo(Nil)
    combine(List(List("a"), List("b"))) must beEqualTo(List(List("a", "b")))
    combine(List(List("a"), List("b", "c"))) must beEqualTo(List(List("a", "b"), List("a", "c")))
    combine(List(List("a", "b"), List("c", "d"))) must beEqualTo(List(List("a", "c"), List("a", "d"), List("b", "c"), List("b", "d")))
  }

  "combine named lists" in {
    combineNamed(List(("n1", Nil))) must beEqualTo(Nil)
    combineNamed(List(("n1", List("a")), ("n2", Nil))) must beEqualTo(Nil)
    combineNamed(List(("n1", Nil), ("n2", List("a")))) must beEqualTo(Nil)
    combineNamed(List(("n1", List("a")), ("n2", List("b")))) must beEqualTo(List(List(("n1", "a"), ("n2", "b"))))
    combineNamed(List(("n1", List("a")), ("n2", List("b", "c")))) must beEqualTo(List(List(("n1", "a"), ("n2", "b")), List(("n1", "a"), ("n2", "c"))))
    combineNamed(List(("n1", List("a", "b")), ("n2", List("c", "d")))) must beEqualTo(List(List(("n1", "a"), ("n2", "c")), List(("n1", "a"), ("n2", "d")), List(("n1", "b"), ("n2", "c")), List(("n1", "b"), ("n2", "d"))))
  }

  private def combine(lists: List[List[String]]) = ListCombiner.combine[String](lists)

  private def combineNamed(lists: List[(String, List[String])]) = ListCombiner.combineNamed[String](lists)
}
