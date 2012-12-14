package com.hlewis.rabbit_scheduler.app

import com.redis.RedisClient
import com.hlewis.rabbit_scheduler.domain.JobStore

trait RedisJobStoreFactory {
  def createJobStore(): JobStore = {
    val redisClient = new RedisClient("localhost", 6379)

    new RedisJobStore(redisClient)
  }
}
