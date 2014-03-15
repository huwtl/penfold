package org.huwtl.penfold.app.support.json

import org.specs2.mutable.Specification

class JsonPathExtractorTest extends Specification {

  val jsonPathExtractor = new JsonPathExtractor

  "should extract json attributes" in {
    val json =
      """
        {
          "single": "hello",
          "obj": {
            "attr": "1"
          },
          "array": [
            {
              "name": "abc",
              "age": 1,
              "innerObj": {
                "inner" : true
              }
            },
            {
              "name": "dEF",
              "age": 2.33
            }
          ],
          "empty" : [],
          "nothing" : null
        }
      """

    jsonPathExtractor.extract(json, "single") must beEqualTo(List("hello"))
    jsonPathExtractor.extract(json, "array / name") must beEqualTo(List("abc", "def"))
    jsonPathExtractor.extract(json, "array / age") must beEqualTo(List("1", "2.33"))
    jsonPathExtractor.extract(json, "array / innerObj / inner") must beEqualTo(List("true"))
    jsonPathExtractor.extract(json, "array / missing") must beEqualTo(Nil)
    jsonPathExtractor.extract(json, "missing") must beEqualTo(Nil)
    jsonPathExtractor.extract(json, "obj") must beEqualTo(Nil)
    jsonPathExtractor.extract(json, "array") must beEqualTo(Nil)
    jsonPathExtractor.extract(json, "empty") must beEqualTo(Nil)
    jsonPathExtractor.extract(json, "nothing") must beEqualTo(Nil)
  }
}
