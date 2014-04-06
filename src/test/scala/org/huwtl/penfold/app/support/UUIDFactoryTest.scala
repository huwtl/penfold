package org.huwtl.penfold.app.support

import org.specs2.mutable.Specification

class UUIDFactoryTest extends Specification {
  val idFactory = new UUIDFactory

  "create unique id" in {
    idFactory.create must not(beEqualTo(idFactory.create))
  }
}
