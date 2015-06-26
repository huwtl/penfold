package com.qmetric.penfold.readstore

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PageResultTest extends Specification with Mockito {

  "know when empty" in {
    new PageResult(List(mock[TaskProjection]), previousPage = None, nextPage = None).isEmpty must beFalse
    new PageResult(Nil, previousPage = None, nextPage = None).isEmpty must beTrue
  }
}
