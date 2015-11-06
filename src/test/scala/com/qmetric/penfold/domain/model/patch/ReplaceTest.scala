package com.qmetric.penfold.domain.model.patch

import org.specs2.matcher.DataTables
import org.specs2.mutable.SpecificationWithJUnit

class ReplaceTest extends SpecificationWithJUnit with DataTables {

  "apply replace operation" in {
    "existing"                                || "operation"                                || "expected"                                |
      Map("a" -> Map("c" -> "2"))             !! Replace("/a/c", Value("3"))                !! Map("a" -> Map("c" -> "3"))               |
      Map("a" -> Map("c" -> "2"))             !! Replace("/a", Value("1"))                  !! Map("a" -> "1")                           |
      Map("list" -> List("a", "b"))           !! Replace("/list/0", Value("c"))             !! Map("list" -> List("c", "b"))             |
      Map("list" -> List("a", "b"))           !! Replace("/list/1", Value("c"))             !! Map("list" -> List("a", "c"))             |
      Map("list" -> List("a", "b"))           !! Replace("/list/1", Value(Map("d" -> "4"))) !! Map("list" -> List("a", Map("d" -> "4"))) |
      Map("list" -> List("a", Map("b" -> 2))) !! Replace("/list/1/b", Value(3))             !! Map("list" -> List("a", Map("b" -> 3)))   |> {
      (existing, operation, expected) =>
        operation.exec(existing.asInstanceOf[Map[String, Any]]) must beEqualTo(expected)
    }
  }

  "apply addition when path item to replace not found" in {
    Replace("/unknown", Value("2")).exec(Map("a" -> "1")) must beEqualTo(Map("a" -> "1", "unknown" -> "2"))
  }
}
