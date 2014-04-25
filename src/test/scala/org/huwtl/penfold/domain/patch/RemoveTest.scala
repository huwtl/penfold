package org.huwtl.penfold.domain.patch

import org.specs2.mutable.Specification
import org.specs2.matcher.DataTables

class RemoveTest extends Specification with DataTables {
  "apply remove operation" in {
    "existing"                       ||  "operation"              || "expected"                  |
      Map("a" -> Map("c" -> "2"))    !!  Remove("/a/c")           !! Map("a" -> Map.empty)       |
      Map("a" -> Map("c" -> "2"))    !!  Remove("/a")             !! Map.empty                   |
      Map("list" -> List("a", "b"))  !!  Remove("/list/0")        !! Map("list" -> List("b"))    |
      Map("list" -> List("a", "b"))  !!  Remove("/list/1")        !! Map("list" -> List("a"))    |> {
      (existing, operation, expected) =>
        operation.exec(existing.asInstanceOf[Map[String, Any]]) must beEqualTo(expected)
    }
  }

  "throw error when invalid path" in {
    Remove("/unknown").exec(Map("a" -> "1")) must throwA[IllegalStateException]
  }
}
