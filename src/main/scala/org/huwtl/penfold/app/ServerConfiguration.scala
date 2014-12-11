package org.huwtl.penfold.app

import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import org.huwtl.penfold.app.readstore.mongodb.Index
import org.huwtl.penfold.readstore.{SortOrderMapping, SortOrder}
import org.huwtl.penfold.domain.model.Status

case class ServerConfiguration(publicUrl: String,
                               httpPort: Int,
                               authentication: Option[AuthenticationCredentials],
                               domainJdbcConnectionPool: JdbcConnectionPool,
                               readStoreMongoDatabaseServers: MongoDatabaseServers,
                               readStoreIndexes: List[Index] = Nil,
                               sortOrdering: SortOrderingConfiguration = SortOrderingConfiguration(),
                               pageSize: Int = 10,
                               eventSync: FiniteDuration = FiniteDuration(15L, TimeUnit.MINUTES),
                               triggeredCheckFrequency: FiniteDuration = FiniteDuration(60L, TimeUnit.SECONDS),
                               taskArchiver: Option[TaskArchiverConfiguration] = None,
                               readyTaskAssignmentTimeout: Option[TaskAssignmentTimeoutConfiguration] = None)

case class AuthenticationCredentials(username: String, password: String)

case class JdbcConnectionPool(url: String, username: String, password: String, driver: String, poolSize: Int = 15)

case class MongoDatabaseServers(databaseName: String, servers: List[MongoDatabaseServer])

case class MongoDatabaseServer(host: String, port: Int)

case class TaskArchiverConfiguration(timeoutPayloadPath: String,
                                     checkFrequency: FiniteDuration = FiniteDuration(60L, TimeUnit.SECONDS))

case class TaskAssignmentTimeoutConfiguration(timeoutPayloadPath: String,
                                              checkFrequency: FiniteDuration = FiniteDuration(60L, TimeUnit.SECONDS))

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