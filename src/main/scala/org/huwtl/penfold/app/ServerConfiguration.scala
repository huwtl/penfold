package org.huwtl.penfold.app

import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import org.huwtl.penfold.app.readstore.mongodb.Index

case class ServerConfiguration(publicUrl: String,
                               httpPort: Int,
                               authentication: Option[AuthenticationCredentials],
                               domainJdbcConnectionPool: JdbcConnectionPool,
                               readStoreMongoDatabaseServers: MongoDatabaseServers,
                               readStoreIndexes: List[Index] = Nil,
                               triggeredCheckFrequency: FiniteDuration = FiniteDuration(60L, TimeUnit.SECONDS))

case class AuthenticationCredentials(username: String, password: String)

case class JdbcConnectionPool(url: String, username: String, password: String, driver: String, poolSize: Int = 15)

case class MongoDatabaseServers(databaseName: String, servers: List[MongoDatabaseServer])
case class MongoDatabaseServer(host: String, port: Int)