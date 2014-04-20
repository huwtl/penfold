package org.huwtl.penfold.app

import org.specs2.mutable.Specification

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.FicusConfig._
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit._
import org.huwtl.penfold.app.readstore.mongodb.{IndexField, Index}

class ServerConfigurationTest extends Specification {

  val publicUrl = "http://localhost:9762"

  val httpPort = 9762

  val authCredentials = AuthenticationCredentials("user", "secret")

  val jdbcUrl = "jdbc:hsqldb:mem:penfold;sql.syntax_mys=true"

  val indexes = List(
    Index(List(IndexField("field1", "payload.field1"))),
    Index(List(IndexField("field1", "payload.field1"), IndexField("field2", "payload.field2"))))

  "load minimally populated config file" in {
    val expectedConfig = ServerConfiguration(publicUrl, httpPort, None, JdbcConnectionPool(jdbcUrl, "user", "", "org.hsqldb.jdbcDriver"),
      MongoDatabaseServers("dbname", List(MongoDatabaseServer("127.0.0.1", 27017))))

    val config = loadConfig("minimal")

    config must beEqualTo(expectedConfig)
  }

  "load fully populated config file" in {
    val expectedConfig = ServerConfiguration(publicUrl, httpPort, Some(authCredentials), JdbcConnectionPool(jdbcUrl, "user", "secret", "org.hsqldb.jdbcDriver", 10),
      MongoDatabaseServers("dbname", List(MongoDatabaseServer("127.0.0.1", 27017), MongoDatabaseServer("127.0.0.2", 27018))),
      readStoreIndexes = indexes, pageSize = 25, triggeredCheckFrequency = FiniteDuration(1L, MINUTES))

    val config = loadConfig("full")

    config must beEqualTo(expectedConfig)
  }

  private def loadConfig(fileName: String) = ConfigFactory.load(s"fixtures/config/$fileName").as[ServerConfiguration]("penfold")
}
