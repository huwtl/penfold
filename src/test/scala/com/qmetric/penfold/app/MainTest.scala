package com.qmetric.penfold.app

import org.specs2.mutable.Specification
import org.specs2.specification.{Step, Fragments}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MainTest extends Specification {
  sequential

  sys.props.put("config.file", getClass.getClassLoader.getResource("application.conf").getPath)

  val server = new Main().init()

  override def map(fs: => Fragments) = fs ^ Step(server.stop())

  "server should start up" in {
    server.isStarted
  }
}
