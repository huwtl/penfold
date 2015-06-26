package com.qmetric.penfold.domain.model.patch

import org.specs2.mutable.Specification
import org.specs2.matcher.DataTables
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PatchTest extends Specification with DataTables {

  "apply patch" in {
    val operations = List(Add("/list/0", Value(Map("a" -> "1", "b" -> "2"))), Remove("/list/0/a"))
    val existing = Map("list" -> List("c"))
    val result = Map("list" -> List(Map("b" -> "2"), "c"))
    Patch(operations).exec(existing) must beEqualTo(result)
  }
}
