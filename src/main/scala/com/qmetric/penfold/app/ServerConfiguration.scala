package com.qmetric.penfold.app

import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import com.qmetric.penfold.app.readstore.mongodb.Index

case class ServerConfiguration(publicUrl: String,
                               httpPort: Int,
                               authentication: Option[AuthenticationCredentials],
                               domainJdbcConnectionPool: JdbcConnectionPool,
                               readStoreMongoDatabaseServers: MongoDatabaseServers,
                               readStoreIndexes: List[Index] = Nil,
                               pageSize: Int = 10,
                               eventSync: FiniteDuration = FiniteDuration(15L, TimeUnit.MINUTES),
                               triggeredCheckFrequency: FiniteDuration = FiniteDuration(60L, TimeUnit.SECONDS),
                               taskArchiver: Option[TaskArchiverConfiguration] = None)

case class AuthenticationCredentials(username: String, password: String)

case class JdbcConnectionPool(url: String, username: String, password: String, driver: String, poolSize: Int = 15)

case class MongoDatabaseServers(databaseName: String, servers: List[MongoDatabaseServer])
case class MongoDatabaseServer(host: String, port: Int)

case class TaskArchiverConfiguration(timeoutAttributePath: String,
                                     checkFrequency: FiniteDuration = FiniteDuration(60L, TimeUnit.SECONDS))