package com.qmetric.penfold.app.support.json

import org.specs2.mutable.Specification
import org.specs2.matcher.DataTables
import com.qmetric.penfold.domain.model.patch.{Replace, Remove, Value, Add}
import com.qmetric.penfold.support.JsonFixtures

class PatchOperationSerializerTest extends Specification with DataTables with JsonFixtures {

  val addOperation = Add("/a/b", Value("1"))
  val addOperationWithNumberic = Add("/a/b", Value(1))
  val addOperationWithComplex = Add("/a/b", Value(Map("complex" -> "1")))
  val removeOperation = Remove("/a/b")
  val replaceOperation = Replace("/a/b", Value("1"))

  val serializer = new PatchOperationSerializer

  "deserialise patch operation" in {
    "jsonPath"                      || "expected"                |
      "add.json"                    !! addOperation              |
      "addWithNumericValue.json"    !! addOperationWithNumberic  |
      "addWithComplexValue.json"    !! addOperationWithComplex   |
      "remove.json"                 !! removeOperation           |
      "replace.json"                !! replaceOperation          |> {
      (jsonPath, expectedEvent) =>
        val json = jsonFixtureAsString(s"fixtures/patch/$jsonPath")
        val actualEvent = serializer.deserialize(json)
        actualEvent must beEqualTo(expectedEvent)
    }
  }

  "serialise patch operation" in {
    "op"                      ||  "expected"                  |
      addOperation              !!  "add.json"                  |
      addOperationWithNumberic  !!  "addWithNumericValue.json"  |
      addOperationWithComplex   !!  "addWithComplexValue.json"  |
      removeOperation           !!  "remove.json"               |
      replaceOperation          !!  "replace.json"              |> {
      (op, expected) =>
        val expectedJson = jsonFixture(s"fixtures/patch/$expected")
        val json = asJson(serializer.serialize(op))
        json must beEqualTo(expectedJson)
    }
  }
}