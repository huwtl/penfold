package com.qmetric.penfold.app

import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import com.qmetric.penfold.readstore.SortOrder
import com.qmetric.penfold.domain.model.Status
import com.qmetric.penfold.app.readstore.postgres.Aliases
import java.util.concurrent.TimeUnit._
import com.qmetric.penfold.app.support.postgres.CustomDbMigrationPath
import com.qmetric.penfold.readstore.SortOrderMapping
import com.qmetric.penfold.app.readstore.postgres.Alias
import scala.Some
import com.qmetric.penfold.app.readstore.postgres.Path

case class ServerConfiguration(publicUrl: String,
                               httpPort: Int,
                               authentication: Option[AuthenticationCredentials],
                               database: DatabaseConfiguration,
                               private val customDbMigrationPath : Option[String] = None,
                               private val queryAliases: Map[String, String] = Map.empty,
                               sortOrdering: SortOrderingConfiguration = SortOrderingConfiguration(),
                               pageSize: Int = 10,
                               triggerCheckFrequency: FiniteDuration = FiniteDuration(60L, TimeUnit.SECONDS),
                               archiver: Option[TaskArchiverConfiguration] = Some(TaskArchiverConfiguration(FiniteDuration(28L, DAYS), FiniteDuration(1L, HOURS))),
                               startedTaskTimeout: Option[StartedTaskTimeoutConfiguration] = Some(StartedTaskTimeoutConfiguration(FiniteDuration(1L, DAYS), FiniteDuration(1L, DAYS)))) {

  val queryPathAliases = Aliases(queryAliases.map {
    case (alias, path) => (Alias(alias), Path(path))
  })

  val dbMigrationPath = customDbMigrationPath.map(CustomDbMigrationPath)
}

case class AuthenticationCredentials(username: String, password: String)

case class DatabaseConfiguration(url: String, username: String, password: String, driver: String = "org.postgresql.Driver", poolSize: Int = 15)

case class TaskArchiverConfiguration(timeout: FiniteDuration, checkFrequency: FiniteDuration = FiniteDuration(30L, TimeUnit.MINUTES))

case class StartedTaskTimeoutConfiguration(timeout: FiniteDuration, checkFrequency: FiniteDuration = FiniteDuration(60L, TimeUnit.SECONDS))

case class SortOrderingConfiguration(private val waiting: String = "Asc",
                                     private val ready: String = "Asc",
                                     private val started: String = "Desc",
                                     private val closed: String = "Desc") {

  val mapping = new SortOrderMapping(Map(
    Status.Waiting -> SortOrder.from(waiting),
    Status.Ready -> SortOrder.from(ready),
    Status.Started -> SortOrder.from(started),
    Status.Closed -> SortOrder.from(closed)
  ))
}