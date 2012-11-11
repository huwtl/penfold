package com.hlewis.rabbit_scheduler.jobstore

import com.redis.RedisClient
import com.google.inject.Inject

class RedisJobstore @Inject()(val client: RedisClient) extends Jobstore {

  override def add(key: String, value: String): Boolean = {
    client.hset("test", key, value)
  }

}
