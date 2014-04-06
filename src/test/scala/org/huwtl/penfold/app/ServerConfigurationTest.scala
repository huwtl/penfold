package org.huwtl.penfold.app

import org.specs2.mutable.Specification

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.FicusConfig._
import org.huwtl.penfold.app.query.redis.{Index, IndexField}
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit._

class ServerConfigurationTest extends Specification {

  val publicUrl = "http://localhost:9762"

  val httpPort = 9762

  val jdbcUrl = "jdbc:hsqldb:mem:penfold;sql.syntax_mys=true"

  val indexes = List(
    Index("index1", List(IndexField("field1", "payload / inner / field1"))),
    Index("index2", List(IndexField("field1", "payload / inner / field1"), IndexField("field2", "payload / field2"))))

  "load fully populated config file with redis domain store" in {
    val expectedConfig = ServerConfiguration(publicUrl, httpPort, None, Some(RedisConnectionPool("localhost", 6380, 0, Some("secret"), 10)),
      RedisConnectionPool("localhost", 6380, 1, Some("secret"), 100), queryIndexes = indexes, triggeredCheckFrequency = FiniteDuration(1L, MINUTES))

    val config = loadConfig("fullWithRedisDomainPool")

    config must beEqualTo(expectedConfig)
    config.domainConnectionPool.right.get must beEqualTo(expectedConfig.domainRedisConnectionPool.get)
    config.domainConnectionPool.isLeft must beFalse
  }

  "load minimally populated config file with redis domain store" in {
    val expectedConfig = ServerConfiguration(publicUrl, httpPort, None, Some(RedisConnectionPool("localhost", 6380, 0, None)),
      RedisConnectionPool("localhost", 6380, 1, None))

    val config = loadConfig("minimalWithRedisDomainPool")

    config must beEqualTo(expectedConfig)
    config.domainConnectionPool.right.get must beEqualTo(expectedConfig.domainRedisConnectionPool.get)
    config.domainConnectionPool.isLeft must beFalse
  }

  "load minimally populated config file with jdbc domain store" in {
    val expectedConfig = ServerConfiguration(publicUrl, httpPort, Some(JdbcConnectionPool(jdbcUrl, "user", "", "org.hsqldb.jdbcDriver")),
      None, RedisConnectionPool("localhost", 6380, 1, None))

    val config = loadConfig("minimalWithJdbcDomainPool")

    config must beEqualTo(expectedConfig)
    config.domainConnectionPool.left.get must beEqualTo(expectedConfig.domainJdbcConnectionPool.get)
    config.domainConnectionPool.isRight must beFalse
  }

  "load fully populated config file with jdbc domain store" in {
    val expectedConfig = ServerConfiguration(publicUrl, httpPort, Some(JdbcConnectionPool(jdbcUrl, "user", "secret", "org.hsqldb.jdbcDriver", 10)),
      None, RedisConnectionPool("localhost", 6380, 1, Some("secret"), 100), queryIndexes = indexes, triggeredCheckFrequency = FiniteDuration(1L, MINUTES))

    val config = loadConfig("fullWithJdbcDomainPool")

    config must beEqualTo(expectedConfig)
    config.domainConnectionPool.left.get must beEqualTo(expectedConfig.domainJdbcConnectionPool.get)
    config.domainConnectionPool.isRight must beFalse
  }

  "error when no domain store" in {
    ServerConfiguration(publicUrl, httpPort, None, None, RedisConnectionPool("localhost", 6380, 1, None)) must throwA[IllegalArgumentException]

  }

  private def loadConfig(fileName: String) = ConfigFactory.load(s"fixtures/config/$fileName").as[ServerConfiguration]("penfold")
}
