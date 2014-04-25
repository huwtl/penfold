package org.huwtl.penfold.domain.patch

import org.specs2.mutable.Specification
import org.specs2.matcher.DataTables

class AddTest extends Specification with DataTables {

  "apply add operation" in {
    "existing"                     || "operation"                       || "expected"                              |
      Map("a" -> Map("c" -> "2"))  !! Add("/a/b", Value("1"))           !! Map("a" -> Map("b" -> "1", "c" -> "2")) |
      Map("a" -> Map("c" -> "2"))  !! Add("/b", Value("1"))             !! Map("a" -> Map("c" -> "2"), "b" -> "1") |
      Map("list" -> List("b"))     !! Add("/list/0", Value("a"))        !! Map("list" -> List("a", "b"))           |
      Map("list" -> List("b"))     !! Add("/list/1", Value("a"))        !! Map("list" -> List("b", "a"))           |
      Map.empty                    !! Add("/b", Value("1"))             !! Map("b" -> "1")                         |
      Map.empty                    !! Add("/b", Value(Map("d" -> "4"))) !! Map("b" -> Map("d" -> "4"))             |> {
      (existing, operation, expected) =>
        operation.exec(existing.asInstanceOf[Map[String, Any]]) must beEqualTo(expected)
    }
  }

  "prevent replacement of existing values" in {
    Add("/a/c", Value("3")).exec(Map("a" -> Map("c" -> "2"))) must throwA[IllegalStateException]
  }

  "throw error when invalid path" in {
    Add("/unknown/b", Value("2")).exec(Map("a" -> "1")) must throwA[IllegalStateException]
  }
}

