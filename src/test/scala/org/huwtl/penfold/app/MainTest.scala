package org.huwtl.penfold.app

import org.specs2.mutable.Specification
import org.specs2.specification.{Step, Fragments}

class MainTest extends Specification {
  sys.props.put("config.file", getClass.getClassLoader.getResource("fixtures/config/fullWithRedisDomainPool.conf").getPath)

  val server = new Main().init()

  override def map(fs: => Fragments) = fs ^ Step(server.stop())

  "server should start up" in {
    server.isStarted
  }
}
