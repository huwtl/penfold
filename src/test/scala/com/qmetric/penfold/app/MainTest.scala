package com.qmetric.penfold.app

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.{Fragments, Step}

class MainTest extends SpecificationWithJUnit {
  sequential

  sys.props.put("config.file", getClass.getClassLoader.getResource("application.conf").getPath)

  val server = new Main().init()

  override def map(fs: => Fragments) = fs ^ Step(server.stop())

  "server should start up" in {
    server.isStarted
  }
}
