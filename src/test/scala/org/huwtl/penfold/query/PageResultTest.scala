package org.huwtl.penfold.query

import org.specs2.mutable.Specification

class PageResultTest extends Specification {

  "know next page number if exists" in {
    new PageResult(0, List(), previousExists = false, nextExists = true).nextPageNumber must beEqualTo(1)
    new PageResult(1, List(), previousExists = true, nextExists = true).nextPageNumber must beEqualTo(2)
    new PageResult(1, List(), previousExists = true, nextExists = false).nextPageNumber must beEqualTo(1)
  }

  "know previous page number if exists" in {
    new PageResult(0, List(), previousExists = false, nextExists = true).previousPageNumber must beEqualTo(0)
    new PageResult(2, List(), previousExists = true, nextExists = true).previousPageNumber must beEqualTo(1)
  }
}
