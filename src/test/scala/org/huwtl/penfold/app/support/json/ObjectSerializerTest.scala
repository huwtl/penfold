package org.huwtl.penfold.app.support.json

import org.specs2.mutable.Specification
import org.specs2.matcher.DataTables
import scala.io.Source._
import org.huwtl.penfold.readstore._
import org.json4s.jackson.JsonMethods._
import org.huwtl.penfold.readstore.GT
import org.huwtl.penfold.readstore.IN
import org.huwtl.penfold.readstore.EQ
import org.huwtl.penfold.readstore.LT

class ObjectSerializerTest extends Specification with DataTables {
  val serializer = new ObjectSerializer

  "deserialise filter" in {
    "jsonPath"                || "expected"                                              |
      "eqFilter.json"         !! EQ("name", "val", QueryParamType.StringType)            |
      "eqFilterMinimal.json"  !! EQ("name", null, QueryParamType.StringType)             |
      "inFilter.json"         !! IN("name", Set("val", null), QueryParamType.StringType) |
      "inFilterMinimal.json"  !! IN("name", Set(), QueryParamType.StringType)            |
      "ltFilter.json"         !! LT("name", "100", QueryParamType.NumericType)           |
      "ltFilterMinimal.json"  !! LT("name", null, QueryParamType.NumericType)            |
      "gtFilter.json"         !! GT("name", "100", QueryParamType.NumericType)           |
      "gtFilterMinimal.json"  !! GT("name", null, QueryParamType.NumericType)            |> {
      (jsonPath, expected) =>
        val json = fromInputStream(getClass.getClassLoader.getResourceAsStream(s"fixtures/filter/$jsonPath")).mkString
        val actualFilter = serializer.deserialize[Filter](json)
        actualFilter must beEqualTo(expected)
    }
  }

  "serialise filter" in {
    "filter"                                                  || "expected"        |
      EQ("name", "val", QueryParamType.StringType)            !! "eqFilter.json"   |
      IN("name", Set("val", null), QueryParamType.StringType) !! "inFilter.json"   |
      LT("name", "100", QueryParamType.NumericType)           !! "ltFilter.json"   |
      GT("name", "100", QueryParamType.NumericType)           !! "gtFilter.json"   |> {
      (filter, expected) =>
        val expectedJson = compact(parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(s"fixtures/filter/$expected")).mkString))
        val actualJson = serializer.serialize(filter)
        actualJson must beEqualTo(expectedJson)
    }
  }
}
