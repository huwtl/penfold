package com.hlewis.eventfire.app.store.redis

import com.redis.RedisClient
import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.FunSpec
import org.scalatest.mock.MockitoSugar
import org.mockito.BDDMockito._
import com.hlewis.eventfire.domain.{Body, Cron, Header, Job}

class RedisJobstoreSpec extends ScalatraSuite with FunSpec with MockitoSugar {

  val redisClient = mock[RedisClient]

  val redisJobstore = new RedisJobStore(redisClient)

  describe("Redis job store") {

    it("should add job") {
      given(redisClient.hset("test", "key", "value")).willReturn(true)

      val result = redisJobstore.add(Job(Header("key", "test", Cron("1", "10", "*", "*", "*"), Map()), Body(Map("data" -> "value"))))

      result should be(true)
    }
  }
}
