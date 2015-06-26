package com.qmetric.penfold.app.support

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UUIDFactoryTest extends Specification {
  val idFactory = new UUIDFactory

  "create unique id" in {
    idFactory.create must not(beEqualTo(idFactory.create))
  }
}
