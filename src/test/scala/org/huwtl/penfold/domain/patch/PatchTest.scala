package org.huwtl.penfold.domain.patch

import org.specs2.mutable.Specification
import org.specs2.matcher.DataTables

class PatchTest extends Specification with DataTables {

  "apply patch" in {
      "existing"                     || "operations"                            || "expected"                              |
        Map("a" -> Map("c" -> "2"))  !! List(Add("/a/b", Value("1")))           !! Map("a" -> Map("b" -> "1", "c" -> "2")) |
        Map("a" -> Map("c" -> "2"))  !! List(Add("/a/c", Value("3")))           !! Map("a" -> Map("c" -> "3"))             |
        Map("a" -> Map("c" -> "2"))  !! List(Add("/b", Value("1")))             !! Map("a" -> Map("c" -> "2"), "b" -> "1") |
        Map.empty                    !! List(Add("/b", Value("1")))             !! Map("b" -> "1")                         |
        Map.empty                    !! List(Add("/b", Value(Map("d" -> "4")))) !! Map("b" -> Map("d" -> "4"))             |> {
        (existing, operations, expected) =>
          Patch(operations).exec(existing.asInstanceOf[Map[String, Any]]) must beEqualTo(expected)
      }
  }
}
