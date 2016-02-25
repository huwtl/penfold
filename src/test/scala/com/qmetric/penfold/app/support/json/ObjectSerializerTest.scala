package com.qmetric.penfold.app.support.json

import com.qmetric.penfold.readstore.{EQ, IN, LT, _}
import com.qmetric.penfold.support.JsonFixtures
import org.joda.time.DateTime
import org.specs2.matcher.DataTables
import org.specs2.mutable.SpecificationWithJUnit

class ObjectSerializerTest extends SpecificationWithJUnit with DataTables with JsonFixtures {
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
        val json = jsonFixtureAsString(s"fixtures/filter/$jsonPath")
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
        val expectedJson = jsonFixture(s"fixtures/filter/$expected")
        val actualJson = asJson(serializer.serialize(filter))
        actualJson must beEqualTo(expectedJson)
    }
  }

  "deserialise dateTime" in {
    "dateTimeString" || "expected" |
      "2016-03-27 01:00:00" !! new DateTime(2016, 3, 27, 2, 0, 0) |
      "2016-02-27 01:00:00" !! new DateTime(2016, 2, 27, 1, 0, 0) |> {
      (dateTimeString, expected) =>
        serializer.deserialize[Map[String, DateTime]]( s"""{"dateTime": "$dateTimeString"}""") must beEqualTo(Map("dateTime" -> expected))
    }
  }

  "serialise dateTime" in {
    "dateTime" || "expected" |
      new DateTime(2016, 3, 27, 2, 0, 0) !! "2016-03-27 02:00:00" |
      new DateTime(2016, 2, 27, 1, 0, 0) !! "2016-02-27 01:00:00" |> {
      (dateTime, expected) =>
        serializer.serialize(Map("dateTime" -> dateTime)) must beEqualTo(s"""{"dateTime":"$expected"}""")
    }
  }
}