package com.hlewis.eventfire.app

import com.redis.RedisClient
import com.hlewis.eventfire.domain.JobStore

class RedisJobStore(client: RedisClient) extends JobStore {

  override def add(key: String, value: String) = {
    client.hset("test", key, value)
  }

}
