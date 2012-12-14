package com.hlewis.rabbit_scheduler.app

import com.redis.RedisClient
import com.hlewis.rabbit_scheduler.domain.JobStore

class RedisJobStore(client: RedisClient) extends JobStore {

  override def add(key: String, value: String) = {
    client.hset("test", key, value)
  }

}
