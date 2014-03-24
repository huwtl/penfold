package org.huwtl.penfold.app

import org.huwtl.penfold.app.query.Index
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit

case class ServerConfiguration(publicUrl: String,
                               httpPort: Int,
                               domainRedisConnectionPool: RedisConnectionPool,
                               queryRedisConnectionPool: RedisConnectionPool,
                               queryIndexes: List[Index] = Nil,
                               triggeredJobCheckFrequency : FiniteDuration = FiniteDuration(30L, TimeUnit.SECONDS))

case class RedisConnectionPool(host: String, port: Int, database: Int, password: Option[String], poolSize: Int = 8)