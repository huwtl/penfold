package org.huwtl.penfold.support

import org.specs2.specification.{Step, Fragments}
import org.specs2.mutable.Specification
import redis.embedded.RedisServer
import com.redis.RedisClientPool

trait RedisSpecification extends Specification {

  sequential

  val redisServerPort = 6380

  val testDatabaseIndex = 2

  val redisServer = new RedisServer(redisServerPort)

  def newRedisClientPool() = {
    val redisClientPool = new RedisClientPool("localhost", redisServerPort, database = testDatabaseIndex)
    redisClientPool.withClient {
      client =>
        client.flushdb
    }
    redisClientPool
  }

  override def map(fs: => Fragments) = Step(redisServer.start()) ^ fs ^ Step(redisServer.stop())
}