package org.huwtl.penfold.app

import org.specs2.mutable.Specification

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.FicusConfig._
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit._

class ServerConfigurationTest extends Specification {

  val publicUrl = "http://localhost:9762"

  val httpPort = 9762

  val authCredentials = AuthenticationCredentials("user", "secret")

  val jdbcUrl = "jdbc:hsqldb:mem:penfold;sql.syntax_mys=true"

  "load minimally populated config file" in {
    val expectedConfig = ServerConfiguration(
      publicUrl,
      httpPort,
      None,
      JdbcConnectionPool(jdbcUrl, "user", "", "org.hsqldb.jdbcDriver"),
      JdbcConnectionPool(jdbcUrl, "user2", "", "org.hsqldb.jdbcDriver")
    )

    val config = loadConfig("minimal")

    config must beEqualTo(expectedConfig)
  }

  "load fully populated config file" in {
    val expectedConfig = ServerConfiguration(
      publicUrl,
      httpPort,
      Some(authCredentials),
      JdbcConnectionPool(jdbcUrl, "user", "secret", "org.hsqldb.jdbcDriver", 10),
      JdbcConnectionPool(jdbcUrl, "user", "secret", "org.hsqldb.jdbcDriver", 10),
      Map("alias1" -> "path1", "alias2" -> "path2"),
      sortOrdering = SortOrderingConfiguration("Desc", "Desc", "Asc", "Asc"),
      pageSize = 25,
      eventSync = FiniteDuration(2L, MINUTES),
      triggeredCheckFrequency = FiniteDuration(1L, MINUTES),
      taskArchiver = Some(TaskArchiverConfiguration("archiveTimeout", FiniteDuration(1L, MINUTES))),
      readyTaskAssignmentTimeout = Some(TaskAssignmentTimeoutConfiguration("assignmentTimeout", FiniteDuration(2L, MINUTES)))
    )

    val config = loadConfig("full")

    config must beEqualTo(expectedConfig)
  }

  private def loadConfig(fileName: String) = ConfigFactory.load(s"fixtures/config/$fileName").as[ServerConfiguration]("penfold")
}
