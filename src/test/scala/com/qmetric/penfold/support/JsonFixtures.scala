package com.qmetric.penfold.support

import org.json4s.jackson.JsonMethods._
import org.specs2.mutable.Specification

import scala.io.Source._

trait JsonFixtures {
  def asJson(json: String) = {
    parse(json).values
  }

  def jsonFixture(filePath: String) = {
    parse(jsonFixtureAsString(filePath)).values
  }

  def jsonFixtureAsString(filePath: String) = {
    fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString
  }
}
