package com.qmetric.penfold.app.support.json

import org.specs2.mutable.Specification
import scala.io.Source._
import org.json4s.jackson.JsonMethods._
import org.specs2.matcher.DataTables
import com.qmetric.penfold.domain.model.patch.{Replace, Remove, Value, Add}

class PatchOperationSerializerTest extends Specification with DataTables {

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
        val json = fromInputStream(getClass.getClassLoader.getResourceAsStream(s"fixtures/patch/$jsonPath")).mkString
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
        val expectedJson = compact(parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(s"fixtures/patch/${expected}")).mkString))
        val json = serializer.serialize(op)
        json must beEqualTo(expectedJson)
    }
  }
}
