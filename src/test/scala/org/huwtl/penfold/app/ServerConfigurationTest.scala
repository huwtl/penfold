package org.huwtl.penfold.app

import org.specs2.mutable.Specification

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.FicusConfig._
import org.huwtl.penfold.app.query.{Index, IndexField}
import org.huwtl.penfold.app.query.Index
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit._

class ServerConfigurationTest extends Specification {

  "load fully populated config file" in {
    val expectedConfig = ServerConfiguration("http://localhost:8080", 8080,
      RedisConnectionPool("localhost", 6379, 0, Some("secret"), 10),
      RedisConnectionPool("localhost", 6379, 1, Some("secret"), 100),
      queryIndexes = List(
        Index("index1", List(IndexField("field1", "inner / field1"))),
        Index("index2", List(IndexField("field1", "inner / field1"), IndexField("field2", "field2")))),
      triggeredJobCheckFrequency = FiniteDuration(1L, MINUTES))

    val config = loadConfig("full")

    config must beEqualTo(expectedConfig)
  }

  "load minimally populated config file" in {
    val expectedConfig = ServerConfiguration("http://localhost:8080", 8080,
      RedisConnectionPool("localhost", 6379, 0, None),
      RedisConnectionPool("localhost", 6379, 1, None))

    val config = loadConfig("minimal")

    config must beEqualTo(expectedConfig)
  }

  private def loadConfig(fileName: String) = ConfigFactory.load(s"fixtures/config/$fileName").as[ServerConfiguration]("penfold")
}
