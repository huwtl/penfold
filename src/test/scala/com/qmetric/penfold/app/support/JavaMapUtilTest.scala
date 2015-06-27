package com.qmetric.penfold.app.support

import com.google.common.collect.{ImmutableList, ImmutableMap}
import org.specs2.mutable.SpecificationWithJUnit

class JavaMapUtilTest extends SpecificationWithJUnit {

  "simple scala map to java map" in {
    val expectedJavaMap = ImmutableMap.of("a", 1, "b", true)

    val javaMap = JavaMapUtil.deepConvertToJavaMap(Map("a" -> 1, "b" -> true))

    javaMap must beEqualTo(expectedJavaMap)
  }

  "scala map with nested map to java map" in {
    val expectedJavaMap = ImmutableMap.of("a", 1, "b", true, "innerMap", ImmutableMap.of("nested", "val"))

    val javaMap = JavaMapUtil.deepConvertToJavaMap(Map("a" -> 1, "b" -> true, "innerMap" -> Map("nested" -> "val")))

    javaMap must beEqualTo(expectedJavaMap)
  }

  "scala map with nested iterable to java map" in {
    val expectedJavaMap = ImmutableMap.of("inner", ImmutableList.of("val3"))

    val javaMap = JavaMapUtil.deepConvertToJavaMap(Map("inner" -> List("val3")))

    javaMap must beEqualTo(expectedJavaMap)
  }

  "scala map with nested iterables to java map" in {
    val expectedJavaMap = ImmutableMap.of("a", 1, "b", true, "inner", ImmutableList.of(ImmutableList.of(ImmutableMap.of("nested", "val"), "val2"), "val3"))

    val javaMap = JavaMapUtil.deepConvertToJavaMap(Map("a" -> 1, "b" -> true, "inner" -> List(List(Map("nested" -> "val"), "val2"), "val3")))

    javaMap must beEqualTo(expectedJavaMap)
  }
}
