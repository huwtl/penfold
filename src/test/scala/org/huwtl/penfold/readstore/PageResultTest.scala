package org.huwtl.penfold.readstore

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito

class PageResultTest extends Specification with Mockito {

  "know when empty" in {
    new PageResult(List(mock[TaskRecord]), previousExists = false, nextExists = false).isEmpty must beFalse
    new PageResult(Nil, previousExists = false, nextExists = false).isEmpty must beTrue
  }
}
