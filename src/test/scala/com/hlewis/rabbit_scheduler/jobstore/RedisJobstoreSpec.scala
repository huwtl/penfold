package com.hlewis.rabbit_scheduler.jobstore

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.redis.RedisClient

class RedisJobstoreSpec extends Specification with Mockito {

  val redisClient = mock[RedisClient]

  val redisJobstore = new RedisJobstore(redisClient)

  "redis job store" should {
    "set value in hash" in {

      redisJobstore.add("key", "value")

      there was one(redisClient).hset("test", "key", "value")
    }
  }

}
