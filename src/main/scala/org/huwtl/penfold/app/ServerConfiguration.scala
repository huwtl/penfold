package org.huwtl.penfold.app

import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import org.huwtl.penfold.app.readstore.redis.Index

case class ServerConfiguration(publicUrl: String,
                               httpPort: Int,
                               authentication: Option[AuthenticationCredentials],
                               domainJdbcConnectionPool: Option[JdbcConnectionPool],
                               domainRedisConnectionPool: Option[RedisConnectionPool],
                               readStoreRedisConnectionPool: RedisConnectionPool,
                               readStoreIndexes: List[Index] = Nil,
                               triggeredCheckFrequency: FiniteDuration = FiniteDuration(30L, TimeUnit.SECONDS)) {

  require(domainJdbcConnectionPool.isDefined || domainRedisConnectionPool.isDefined, "No domain connection pool specified")

  def domainConnectionPool: Either[JdbcConnectionPool, RedisConnectionPool] = {
    domainJdbcConnectionPool match {
      case Some(jdbcPool) => Left(domainJdbcConnectionPool.get)
      case None => Right(domainRedisConnectionPool.get)
    }
  }
}

case class AuthenticationCredentials(username: String, password: String)

case class RedisConnectionPool(host: String, port: Int, database: Int, password: Option[String], poolSize: Int = 8)

case class JdbcConnectionPool(url: String, username: String, password: String, driver: String, poolSize: Int = 15)