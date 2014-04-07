package org.huwtl.penfold.readstore

import org.specs2.mutable.Specification

class PageRequestTest extends Specification {

  "calculate start offset" in {
    PageRequest(0, 10).start must beEqualTo(0)
    PageRequest(1, 10).start must beEqualTo(10)
    PageRequest(2, 10).start must beEqualTo(20)
  }

  "calculate end offset" in {
    PageRequest(0, 10).end must beEqualTo(10)
    PageRequest(1, 10).end must beEqualTo(20)
    PageRequest(2, 10).end must beEqualTo(30)
  }

  "know when first page" in {
    PageRequest(0, 10).isFirstPage must beTrue
    PageRequest(1, 10).isFirstPage must beFalse
  }
}
