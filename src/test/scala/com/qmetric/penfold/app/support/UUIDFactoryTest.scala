package com.qmetric.penfold.app.support

import org.specs2.mutable.SpecificationWithJUnit

class UUIDFactoryTest extends SpecificationWithJUnit {
  val idFactory = new UUIDFactory

  "create unique id" in {
    idFactory.create must not(beEqualTo(idFactory.create))
  }
}
