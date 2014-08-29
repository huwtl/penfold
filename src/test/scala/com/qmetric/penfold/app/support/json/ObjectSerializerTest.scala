package com.qmetric.penfold.app.support.json

import org.specs2.mutable.Specification
import org.specs2.matcher.DataTables
import scala.io.Source._
import com.qmetric.penfold.readstore._
import org.json4s.jackson.JsonMethods._
import com.qmetric.penfold.readstore.GreaterThan
import com.qmetric.penfold.readstore.In
import com.qmetric.penfold.readstore.Equals
import com.qmetric.penfold.readstore.LessThan

class ObjectSerializerTest extends Specification with DataTables {
  val serializer = new ObjectSerializer

  "deserialise filter" in {
    "jsonPath"                || "expected"                                              |
      "eqFilter.json"         !! Equals("name", "val", QueryParamType.StringType)        |
      "eqFilterMinimal.json"  !! Equals("name", null, QueryParamType.StringType)         |
      "inFilter.json"         !! In("name", Set("val", null), QueryParamType.StringType) |
      "inFilterMinimal.json"  !! In("name", Set(), QueryParamType.StringType)            |
      "ltFilter.json"         !! LessThan("name", "100", QueryParamType.NumericType)     |
      "ltFilterMinimal.json"  !! LessThan("name", null, QueryParamType.NumericType)      |
      "gtFilter.json"         !! GreaterThan("name", "100", QueryParamType.NumericType)  |
      "gtFilterMinimal.json"  !! GreaterThan("name", null, QueryParamType.NumericType)   |> {
      (jsonPath, expected) =>
        val json = fromInputStream(getClass.getClassLoader.getResourceAsStream(s"fixtures/filter/$jsonPath")).mkString
        val actualFilter = serializer.deserialize[Filter](json)
        actualFilter must beEqualTo(expected)
    }
  }

  "serialise filter" in {
    "filter"                                                  || "expected"        |
      Equals("name", "val", QueryParamType.StringType)        !! "eqFilter.json"   |
      In("name", Set("val", null), QueryParamType.StringType) !! "inFilter.json"   |
      LessThan("name", "100", QueryParamType.NumericType)     !! "ltFilter.json"   |
      GreaterThan("name", "100", QueryParamType.NumericType)  !! "gtFilter.json"   |> {
      (filter, expected) =>
        val expectedJson = compact(parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(s"fixtures/filter/$expected")).mkString))
        val actualJson = serializer.serialize(filter)
        actualJson must beEqualTo(expectedJson)
    }
  }
}
