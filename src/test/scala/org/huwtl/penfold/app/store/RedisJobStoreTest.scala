package org.huwtl.penfold.app.store

import org.specs2.mutable.{BeforeAfter, Specification}
import redis.embedded.RedisServer
import com.redis.RedisClient

class RedisJobStoreTest extends Specification {
  val redisServer = new RedisServer(6379)

  case class context() extends BeforeAfter {
    def before = {
      redisServer.start()
    }
    def after  = {
      redisServer.stop()
    }
  }

  "set key value" in new context {
    val redisClient = new RedisClient("localhost", 6379)

    redisClient.set("test", "abc")

    redisClient.get("test") must beEqualTo(Some("abc"))
  }
}
