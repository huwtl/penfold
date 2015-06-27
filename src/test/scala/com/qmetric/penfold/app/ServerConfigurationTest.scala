package com.qmetric.penfold.app

import java.util.concurrent.TimeUnit._

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.specs2.mutable.SpecificationWithJUnit

import scala.concurrent.duration.FiniteDuration

class ServerConfigurationTest extends SpecificationWithJUnit {

  val publicUrl = "http://localhost:9762"

  val httpPort = 9762

  val authCredentials = AuthenticationCredentials("user", "secret")

  val dbUrl = "jdbc:hsqldb:mem:penfold"

  "load minimally populated config file" in {
    val expectedConfig = ServerConfiguration(
      publicUrl,
      httpPort,
      None,
      DatabaseConfiguration(dbUrl, "user", "", "org.postgresql.Driver")
    )

    val config = loadConfig("minimal")

    config must beEqualTo(expectedConfig)
  }

  "load fully populated config file" in {
    val expectedConfig = ServerConfiguration(
      publicUrl,
      httpPort,
      Some(authCredentials),
      DatabaseConfiguration(dbUrl, "user", "secret", "org.hsqldb.jdbcDriver", 10),
      Some("/tmp"),
      Map("alias1" -> "path1", "alias2" -> "path2"),
      sortOrdering = SortOrderingConfiguration("Desc", "Desc", "Asc", "Asc"),
      pageSize = 25,
      triggerCheckFrequency = FiniteDuration(1L, MINUTES),
      archiver = Some(TaskArchiverConfiguration(FiniteDuration(10L, DAYS), FiniteDuration(1L, MINUTES))),
      startedTaskTimeout = Some(StartedTaskTimeoutConfiguration(FiniteDuration(30L, MINUTES), FiniteDuration(2L, MINUTES)))
    )

    val config = loadConfig("full")

    config must beEqualTo(expectedConfig)
  }

  private def loadConfig(fileName: String) = ConfigFactory.load(s"fixtures/config/$fileName").as[ServerConfiguration]("penfold")
}
