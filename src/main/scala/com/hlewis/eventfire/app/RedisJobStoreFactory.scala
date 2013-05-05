package com.hlewis.eventfire.app

import com.redis.RedisClient
import com.hlewis.eventfire.domain.JobStore

trait RedisJobStoreFactory {
  def createJobStore(): JobStore = {
    val redisClient = new RedisClient("localhost", 6379)

    new RedisJobStore(redisClient)
  }
}
