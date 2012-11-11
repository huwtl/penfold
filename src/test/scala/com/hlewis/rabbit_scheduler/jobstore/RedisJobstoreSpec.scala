package com.hlewis.rabbit_scheduler.jobstore

import com.redis.RedisClient
import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.FunSpec
import org.scalatest.mock.MockitoSugar
import org.mockito.BDDMockito._

class RedisJobstoreSpec extends ScalatraSuite with FunSpec with MockitoSugar {

  val redisClient = mock[RedisClient]

  val redisJobstore = new RedisJobstore(redisClient)

  describe("Redis job store") {

    it("should add job") {
      given(redisClient.hset("test", "key", "value")).willReturn(true)

      val result = redisJobstore.add("key", "value")

      result should be(true)
    }
  }
}
