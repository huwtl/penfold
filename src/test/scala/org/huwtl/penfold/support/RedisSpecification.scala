package org.huwtl.penfold.support

import org.specs2.specification.{Step, Fragments}
import org.specs2.mutable.Specification
import redis.embedded.RedisServer
import com.redis.RedisClient

trait RedisSpecification extends Specification {

  val redisServerPort = 6380
  val redisServer = new RedisServer(redisServerPort)

  sequential

  def newRedisClient() = {
    val redisClient = new RedisClient("localhost", redisServerPort)
    redisClient.flushdb
    redisClient
  }

  override def map(fs: => Fragments) = Step(redisServer.start()) ^ fs ^ Step(redisServer.stop())
}